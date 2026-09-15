const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');

const root = path.resolve(__dirname, '../../..');
const serverDir = path.join(root, 'run/paper/26.3');
const buildDir = path.join(root, 'build/probe-26.3');
const classesDir = path.join(buildDir, 'classes');
const properties = fs.readFileSync(path.join(root, 'gradle.properties'), 'utf8');
const version = /^fullVersion=(.+)$/m.exec(properties)[1].trim();
const snapshot = /^snapshot=true\s*$/m.test(properties) ? '-SNAPSHOT' : '';
const pluginJar = path.resolve(process.argv[2]
    || path.join(root, `build/libs/packetevents-spigot-${version}${snapshot}.jar`));
const paperJar = path.join(serverDir, 'versions/26.3/paper-26.3.jar');

function jars(directory) {
    return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
        const file = path.join(directory, entry.name);
        return entry.isDirectory() ? jars(file) : entry.name.endsWith('.jar') ? [file] : [];
    });
}

function run(command, args) {
    const result = spawnSync(command, args, { cwd: root, windowsHide: true, encoding: 'utf8' });
    fs.appendFileSync(path.join(buildDir, 'compile.log'), (result.stdout || '') + (result.stderr || ''));
    if (result.error) throw result.error;
    if (result.status !== 0) throw new Error(`${command} failed: ${(result.stderr || '').slice(-2000)}`);
}

for (const file of [pluginJar, paperJar]) {
    if (!fs.existsSync(file)) throw new Error(`Missing ${file}; see integrationTest/README.md`);
}
fs.mkdirSync(classesDir, { recursive: true });
fs.writeFileSync(path.join(buildDir, 'compile.log'), '');
const classpath = [pluginJar, paperJar, ...jars(path.join(serverDir, 'libraries'))].join(path.delimiter);
run('javac', ['-cp', classpath, '-d', classesDir, path.join(__dirname, 'java/PacketEvents26_3Probe.java')]);
fs.writeFileSync(path.join(classesDir, 'plugin.yml'), [
    'name: PacketEvents26_3Probe',
    'version: 1.0.0',
    'main: PacketEvents26_3Probe',
    "api-version: '26.3'",
    'depend: [packetevents]',
    ''
].join('\n'));
fs.mkdirSync(path.join(serverDir, 'plugins'), { recursive: true });
run('jar', ['cf', path.join(serverDir, 'plugins/PacketEvents26_3Probe.jar'), '-C', classesDir, '.']);
fs.copyFileSync(pluginJar, path.join(serverDir, 'plugins/packetevents.jar'));
console.log(`Installed integration probe and ${path.basename(pluginJar)} in ${serverDir}`);
