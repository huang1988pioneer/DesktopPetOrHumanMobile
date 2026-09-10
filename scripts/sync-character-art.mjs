// SVG masters are the shared source of truth. No image libraries or runtime SVG
// parsers are needed: Android receives paths; Xcode compiles the original SVGs.
import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = fileURLToPath(new URL('../', import.meta.url));
const source = join(root, 'art', 'characters');
const android = join(root, 'app', 'src', 'main', 'res', 'drawable');
const ios = join(root, 'iOS', 'DesktopPetOrHumanMobile', 'CharacterArt.xcassets');
const roster = JSON.parse(await readFile(join(source, 'roster.json'), 'utf8'));
const check = process.argv.includes('--check');
const failures = [];
const supported = new Set(['id', 'd', 'fill', 'stroke', 'stroke-width', 'stroke-linecap', 'stroke-linejoin', 'fill-opacity', 'stroke-opacity']);
const xmlEscape = value => value.replaceAll('&', '&amp;').replaceAll('"', '&quot;').replaceAll('<', '&lt;');
const json = data => JSON.stringify(data, null, 2) + '\n';

async function output(file, contents) {
  let current;
  try { current = await readFile(file, 'utf8'); } catch (error) { if (error.code !== 'ENOENT') throw error; }
  if (current === contents) return;
  if (check) failures.push(file.slice(root.length));
  else { await mkdir(dirname(file), { recursive: true }); await writeFile(file, contents); }
}

function vector(svg, name) {
  if (!svg.includes('viewBox="0 0 400 400"')) throw new Error(`${name}: expected the shared square viewport.`);
  for (const tag of svg.matchAll(/<([A-Za-z][\w:-]*)\b/g)) {
    if (!['svg', 'title', 'desc', 'path'].includes(tag[1])) throw new Error(`${name}: unsupported SVG element ${tag[1]}; expand it to paths first.`);
  }
  const paths = [...svg.matchAll(/<path\s+([^>]+)\/>/g)].map(match => {
    const attrs = Object.fromEntries([...match[1].matchAll(/([\w-]+)="([^"]*)"/g)].map(attr => [attr[1], attr[2]]));
    for (const key of Object.keys(attrs)) if (!supported.has(key)) throw new Error(`${name}: unsupported path attribute ${key}.`);
    if (!attrs.d || /[^MmZzLlHhVvCcSsQqTtAaEe0-9.,+\s-]/.test(attrs.d)) throw new Error(`${name}: invalid path data.`);
    const result = { name: attrs.id, pathData: attrs.d, fillColor: attrs.fill === 'none' ? '#00000000' : attrs.fill ?? '#000000' };
    if (attrs.stroke && attrs.stroke !== 'none') Object.assign(result, { strokeColor: attrs.stroke, strokeWidth: attrs['stroke-width'] ?? '1', strokeLineCap: attrs['stroke-linecap'] ?? 'butt', strokeLineJoin: attrs['stroke-linejoin'] ?? 'miter' });
    if (attrs['fill-opacity']) result.fillAlpha = attrs['fill-opacity'];
    if (attrs['stroke-opacity']) result.strokeAlpha = attrs['stroke-opacity'];
    return `    <path ${Object.entries(result).filter(([, value]) => value !== undefined).map(([key, value]) => `android:${key}="${xmlEscape(value)}"`).join(' ')}/>`;
  });
  if (!paths.length) throw new Error(`${name}: empty SVG.`);
  return `<?xml version="1.0" encoding="utf-8"?>\n<!-- Generated from art/characters/${name}.svg; run node scripts/sync-character-art.mjs. -->\n<vector xmlns:android="http://schemas.android.com/apk/res/android"\n    android:width="400dp" android:height="400dp"\n    android:viewportWidth="400" android:viewportHeight="400">\n${paths.join('\n')}\n</vector>\n`;
}

const sourceIds = roster.characters.map(c => c.id);
for (const [relative, pattern] of [
  ['app/src/main/java/com/pioneer/desktoppetorhumanmobile/CharacterRoster.java', /static final String\[\] IDS = \{([\s\S]*?)\};/],
  ['iOS/DesktopPetOrHumanMobile/App.swift', /static let all: \[Companion\] = \[([\s\S]*?)\n    \]/],
]) {
  const code = await readFile(join(root, relative), 'utf8');
  const block = code.match(pattern)?.[1];
  const ids = block && [...block.matchAll(relative.endsWith('.java') ? /"([a-z]+)"/g : /id: "([a-z]+)"/g)].map(m => m[1]);
  if (JSON.stringify(ids) !== JSON.stringify(sourceIds)) throw new Error(`Character roster differs in ${relative}.`);
}

await output(join(ios, 'Contents.json'), json({ info: { author: 'xcode', version: 1 } }));
const sheets = new Map(roster.moods.map(mood => [mood, []]));
let count = 0;
for (const [index, character] of roster.characters.entries()) {
  for (const mood of roster.moods) {
    const name = `${character.id}_human_${mood}`;
    const svg = await readFile(join(source, `${name}.svg`), 'utf8');
    await output(join(android, `vector_${name}.xml`), vector(svg, name));
    const imageSet = join(ios, `vector_${name}.imageset`);
    await output(join(imageSet, `${name}.svg`), svg);
    await output(join(imageSet, 'Contents.json'), json({ images: [{ filename: `${name}.svg`, idiom: 'universal' }], info: { author: 'xcode', version: 1 }, properties: { 'preserves-vector-representation': true, 'template-rendering-intent': 'original' } }));
    // Contact sheets are review artifacts only; native apps use individual files.
    const x = 30 + (index % 4) * 310;
    const y = 116 + Math.floor(index / 4) * 332;
    const body = svg.replace(/<svg[^>]*>/, '').replace(/<\/svg>\s*$/, '').replace(/\s*<(title|desc)\b[^>]*>[\s\S]*?<\/\1>/g, '').replace(/\sid="[^"]*"/g, '');
    sheets.get(mood).push(`<svg x="${x}" y="${y}" width="300" height="300" viewBox="0 0 400 400">${body}</svg>\n<text x="${x + 150}" y="${y + 313}" text-anchor="middle">${character.name}</text>`);
    count++;
  }
}
for (const [mood, characters] of sheets) {
  const label = { idle: '待機', happy: '開心', sleep: '睡眠' }[mood];
  const sheet = `<svg xmlns="http://www.w3.org/2000/svg" width="1300" height="1160" viewBox="0 0 1300 1160" role="img" aria-label="12 位人物 SVG 重繪：${label}">\n<rect width="1300" height="1160" fill="#F6F1E8"/>\n<g font-family="Noto Sans TC, Microsoft JhengHei, sans-serif" fill="#433634">\n<text x="52" y=" sixty" font-size="30" font-weight="700">小小夥伴，新的模樣。</text>\n<text x="53" y="91" font-size="16" fill="#81736A">12 位人物 · ${label} · SVG 向量重繪</text>\n<path d="M52 110 H1248" stroke="#D8CDBE"/>\n<g font-size="18">\n${characters.join('\n')}\n</g>\n<text x="650" y="1130" text-anchor="middle" font-size="13" fill="#81736A">DesktopPetOrHumanMobile · 人形角色</text>\n</g>\n</svg>\n`.replace('y=" sixty"', 'y="60"');
  await output(join(root, 'art', `character-sheet-${mood}.svg`), sheet);
}
if (failures.length) {
  console.error(`Out-of-date generated assets (${failures.length}):\n${failures.join('\n')}\nRun node scripts/sync-character-art.mjs.`);
  process.exitCode = 1;
} else console.log(`${check ? 'Verified' : 'Synced'} ${count} SVGs, Android vectors, iOS vector image sets, and all roster IDs.`);
