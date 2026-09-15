const { spawn } = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');
const net = require('node:net');
const zlib = require('node:zlib');

const root = path.resolve(__dirname, '../../..');
const serverDir = path.join(root, 'run/paper/26.3');
const port = 25633;
if (!/^eula=true\s*$/m.test(fs.readFileSync(path.join(serverDir, 'eula.txt'), 'utf8'))) {
    throw new Error('Accept the Minecraft EULA in the isolated test server before running this test.');
}
const propertiesPath = path.join(serverDir, 'server.properties');
let properties = fs.readFileSync(propertiesPath, 'utf8');
for (const [key, value] of Object.entries({
    'server-ip': '127.0.0.1', 'server-port': port, 'online-mode': false,
    'enforce-secure-profile': false, 'white-list': false, 'allow-flight': true,
    'view-distance': 2, 'simulation-distance': 2
})) {
    const pattern = new RegExp(`^${key}=.*$`, 'm');
    properties = pattern.test(properties)
        ? properties.replace(pattern, `${key}=${value}`) : `${properties}\n${key}=${value}\n`;
}
fs.writeFileSync(propertiesPath, properties);

const report = require('./protocol-777.json');
const names = Object.fromEntries(Object.entries(report).map(([state, directions]) => [state,
    Object.fromEntries(Object.entries(directions.clientbound)
        .map(([name, entry]) => [entry.protocol_id, name.replace('minecraft:', '')]))
]));
const packetId = (state, name) => report[state].serverbound[`minecraft:${name}`].protocol_id;
const log = fs.openSync(path.join(root, 'build/probe-server.log'), 'w');
const server = spawn('java', [
    '-Xms512m', '-Xmx2g', '-Dterminal.jline=false', '-Dterminal.ansi=false',
    '-jar', 'paper-26.3-5.jar', '--nogui'
], { cwd: serverDir, windowsHide: true, stdio: ['pipe', 'pipe', 'pipe'] });

let ready = false;
let started = false;
let serverReady = false;
let stopping = false;
let joined = false;
let loaded = false;
let completed = false;
let sawError = false;
let resultPassed = false;
let socket;
let outputPending = '';
const timeout = setTimeout(() => stop('timeout'), 120000);

function stop(reason) {
    if (stopping) return;
    stopping = true;
    completed = reason === 'play checks completed';
    console.log(`STOP ${reason}`);
    socket?.destroy();
    server.stdin.write('stop\n');
    setTimeout(() => {
        if (server.exitCode === null) server.kill();
    }, 20000).unref();
}

function output(data) {
    fs.writeSync(log, data);
    outputPending += data.toString();
    const lines = outputPending.split(/\r?\n/);
    outputPending = lines.pop();
    for (const line of lines) {
        sawError ||= /\bERROR\]/.test(line);
        resultPassed ||= /PROBE_RESULT failures=0 livePackets=[1-9]/.test(line);
        ready ||= line.includes('PROBE_READY');
        serverReady ||= line.includes('Done (');
        if (line.includes('PROBE_') || line.includes('_OK ')) console.log(line);
    }
    if (ready && serverReady && !started) {
        started = true;
        client();
    }
}

server.stdout.on('data', output);
server.stderr.on('data', output);
server.on('error', error => {
    clearTimeout(timeout);
    fs.closeSync(log);
    console.error(error.message);
    process.exitCode = 1;
});
server.on('exit', code => {
    stopping = true;
    clearTimeout(timeout);
    fs.closeSync(log);
    socket?.destroy();
    console.log(`SERVER_EXIT ${code} JOINED ${joined}`);
    process.exitCode = code === 0 && joined && completed && resultPassed && !sawError ? 0 : 1;
});

function varInt(value) {
    const bytes = [];
    do {
        const byte = value & 127;
        value >>>= 7;
        bytes.push(byte | (value ? 128 : 0));
    } while (value);
    return Buffer.from(bytes);
}

function readVarInt(buffer) {
    let value = 0;
    for (let index = 0; index < Math.min(5, buffer.length); index++) {
        const byte = buffer[index];
        value |= (byte & 127) << (7 * index);
        if (!(byte & 128)) return [value, index + 1];
    }
    if (buffer.length >= 5) throw new Error('Invalid VarInt');
    return null;
}

function string(value) {
    const bytes = Buffer.from(value);
    return Buffer.concat([varInt(bytes.length), bytes]);
}

function client() {
    let state = 'login';
    let pending = Buffer.alloc(0);
    let compression = -1;
    function send(id, ...parts) {
        let payload = Buffer.concat([varInt(id), ...parts]);
        if (compression >= 0) {
            payload = payload.length >= compression
                ? Buffer.concat([varInt(payload.length), zlib.deflateSync(payload)])
                : Buffer.concat([varInt(0), payload]);
        }
        socket.write(Buffer.concat([varInt(payload.length), payload]));
    }
    socket = net.createConnection({ host: '127.0.0.1', port }, () => {
        const portBytes = Buffer.alloc(2);
        portBytes.writeUInt16BE(port);
        send(0, varInt(777), string('localhost'), portBytes, varInt(2));
        send(0, string('PEProbe'), Buffer.alloc(16, 1));
    });
    socket.on('error', error => stop(error.message));
    socket.on('close', () => { if (!stopping) stop('client disconnected'); });
    socket.on('data', chunk => {
        try {
            pending = Buffer.concat([pending, chunk]);
            while (!stopping) {
                const length = readVarInt(pending);
                if (!length) break;
                if (length[0] < 1 || length[0] > 8 * 1024 * 1024) throw new Error('Invalid packet size');
                if (pending.length < length[1] + length[0]) break;
                let frame = pending.subarray(length[1], length[1] + length[0]);
                pending = pending.subarray(length[1] + length[0]);
                if (compression >= 0) {
                    const [size, offset] = readVarInt(frame);
                    frame = frame.subarray(offset);
                    if (size) frame = zlib.inflateSync(frame, { maxOutputLength: 8 * 1024 * 1024 });
                }
                const [id, offset] = readVarInt(frame);
                const payload = frame.subarray(offset);
                if (state === 'login') {
                    if (id === 3) compression = readVarInt(payload)[0];
                    else if (id === 2) { send(3); state = 'configuration'; }
                    else throw new Error(`Unexpected login packet ${id}`);
                    continue;
                }
                const name = names[state][id];
                if (name === 'keep_alive') send(packetId(state, 'keep_alive'), payload);
                else if (name === 'ping') send(packetId(state, 'pong'), payload);
                else if (name === 'disconnect') throw new Error('Server disconnected the test client');
                else if (state === 'configuration') {
                    if (name === 'select_known_packs') send(packetId(state, name), varInt(0));
                    if (name === 'finish_configuration') {
                        send(packetId(state, name));
                        state = 'play';
                    }
                } else if (name === 'login' && !joined) {
                    joined = true;
                    console.log('JOINED_PLAY');
                    setTimeout(() => { if (!stopping) exercisePlay(); }, 1500);
                    setTimeout(() => stop('play checks completed'), 12000);
                } else if (name === 'player_position') {
                    const [teleportId, start] = readVarInt(payload);
                    const response = Buffer.alloc(32);
                    payload.copy(response, 0, start, start + 24);
                    payload.copy(response, 24, start + 48, start + 56);
                    send(packetId('play', 'accept_teleportation'), varInt(teleportId), response);
                    if (!loaded) { send(packetId('play', 'player_loaded')); loaded = true; }
                }
            }
        } catch (error) {
            stop(error.message);
        }
    });
}

function exercisePlay() {
    server.stdin.write([
        'give PEProbe minecraft:poplar_stairs',
        'give PEProbe minecraft:red_cushion',
        'give PEProbe minecraft:decorated_pot',
        'execute at PEProbe run particle minecraft:orange_poplar_leaves ~ ~ ~ 0 0 0 0 5 force PEProbe',
        'gamemode creative PEProbe',
        'tp PEProbe 4 6 4',
        'setblock 4 5 4 minecraft:stone',
        'summon minecraft:cushion 4 6 4 {block_pos:[I;4,6,4]}',
        'summon minecraft:poplar_boat 5 6 4',
        'summon minecraft:tnt 4 6 4 {fuse:20}',
        ''
    ].join('\n'));
}
