// Hand-drawn character geometry. Run only to regenerate the original SVG masters;
// use sync-character-art.mjs after editing a master directly.
import { mkdir, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { join } from 'node:path';

const root = fileURLToPath(new URL('../', import.meta.url));
const destination = join(root, 'art', 'characters');
export const roster = [
  { id: 'fengge', name: '鋒哥', hair: 'swept', outfit: 'suit', glasses: '#34343A', accent: '#F68C32', iris: '#96633E' },
  { id: 'fengxiong', name: '鋒兄', hair: 'spiky', outfit: 'jacket', glasses: '#605548', accent: '#405B77', iris: '#96633E', beard: true },
  { id: 'gugugaga', name: '咕咕嘎嘎', hair: 'bob', outfit: 'blackDress', accent: '#F5BB3F', iris: '#617F88', lashes: true },
  { id: 'laowangmao', name: '老王貓', hair: 'cream', outfit: 'fortune', accent: '#D65341', iris: '#956B44' },
  { id: 'miaobaibai', name: '喵白白', hair: 'whiteHood', outfit: 'hoodie', accent: '#819B79', iris: '#65865D', lashes: true },
  { id: 'miaobubu', name: '喵布布', hair: 'calico', outfit: 'creamDress', accent: '#E4A054', iris: '#75914D', lashes: true },
  { id: 'miaoniang', name: '喵娘', hair: 'calicoHood', outfit: 'hoodDress', accent: '#ECA452', iris: '#879A4D', lashes: true },
  { id: 'tuge', name: '兔哥', hair: 'tousled', outfit: 'orangeTee', accent: '#F8812C', iris: '#A4783F' },
  { id: 'xiaotu', name: '小兔', hair: 'streaked', outfit: 'stripeTee', accent: '#F8812C', iris: '#A4783F', smileEyes: true },
  { id: 'xiaoying', name: '小影', hair: 'parted', outfit: 'navySuit', glasses: '#7A8D98', accent: '#3B5369', iris: '#956C42' },
  { id: 'yamei', name: '雅美', hair: 'redTails', outfit: 'redBowDress', accent: '#D94C46', iris: '#A96334', lashes: true },
  { id: 'yumei', name: '玉美', hair: 'long', outfit: 'sailor', accent: '#9EBFE2', iris: '#5D84B1', lashes: true },
];
const moods = ['idle', 'happy', 'sleep'];
const moodLabels = { idle: '待機', happy: '開心', sleep: '睡眠' };
const ink = '#433634';
const skin = '#FFD7B8';
const skinEdge = '#BE8468';
const cream = '#FFF5E4';
let paths = [];
let layer = 'body';
let serial = 0;

function path(d, fill = 'none', stroke = ink, width = 2.6, opacity = 1) {
  const attrs = { id: `${layer}-${++serial}`, d, fill };
  if (stroke !== 'none') Object.assign(attrs, { stroke, 'stroke-width': width, 'stroke-linecap': 'round', 'stroke-linejoin': 'round' });
  if (opacity !== 1) attrs['fill-opacity'] = opacity;
  paths.push(`  <path ${Object.entries(attrs).map(([key, value]) => `${key}="${value}"`).join(' ')}/>`);
}
function ellipse(x, y, rx, ry, fill, stroke = 'none', width = 2.6, opacity = 1) {
  const k = 0.55228475;
  const f = v => Number(v.toFixed(3));
  path(`M${x - rx} ${y} C${x - rx} ${f(y - ry * k)} ${f(x - rx * k)} ${y - ry} ${x} ${y - ry} C${f(x + rx * k)} ${y - ry} ${x + rx} ${f(y - ry * k)} ${x + rx} ${y} C${x + rx} ${f(y + ry * k)} ${f(x + rx * k)} ${y + ry} ${x} ${y + ry} C${f(x - rx * k)} ${y + ry} ${x - rx} ${f(y + ry * k)} ${x - rx} ${y}Z`, fill, stroke, width, opacity);
}
function line(d, stroke = ink, width = 2.6) { path(d, 'none', stroke, width); }
function bow(x, y, color, edge = ink) {
  path(`M${x} ${y} Q${x - 26} ${y - 19} ${x - 27} ${y - 10} L${x - 25} ${y + 13} Q${x - 15} ${y + 17} ${x} ${y} Q${x + 24} ${y - 20} ${x + 27} ${y - 10} L${x + 25} ${y + 13} Q${x + 14} ${y + 15} ${x} ${y}Z`, color, edge, 2);
  ellipse(x, y, 7, 7, color, edge, 2);
}

function rearHair(c) {
  layer = 'hair-back';
  if (c.hair === 'long') {
    path('M118 130 C107 169 110 227 91 279 Q79 308 96 331 L85 341 Q111 346 119 323 Q116 351 135 350 L130 336 Q150 349 157 328 L245 325 Q260 348 276 332 Q275 353 293 344 L287 329 Q313 340 314 318 C281 280 291 206 281 145Z', '#303038');
    path('M126 177 C121 245 129 275 108 313 Q125 301 134 269 L148 189Z', '#49454D', 'none');
    line('M272 181 C272 240 263 292 287 320 M120 226 Q121 277 104 297', '#5B535B', 3);
  } else if (c.hair === 'redTails') {
    path('M119 104 C83 90 83 143 90 177 C96 215 60 242 84 268 Q94 282 80 294 Q113 300 119 273 C130 244 103 227 119 198 L143 126Z', '#D84440', '#783B3B');
    path('M281 104 C317 90 317 143 310 177 C304 215 340 242 316 268 Q306 282 320 294 Q287 300 281 273 C270 244 297 227 281 198 L257 126Z', '#D84440', '#783B3B');
    path('M110 133 C97 180 116 198 93 245 Q84 260 98 265 C94 242 127 223 118 184Z', '#F36B57', 'none');
    path('M290 133 C303 180 284 198 307 245 Q316 260 302 265 C306 242 273 223 282 184Z', '#F36B57', 'none');
    line('M99 142 Q98 167 103 184 M300 142 Q305 178 294 201', '#FFAD80', 3);
  } else if (['bob', 'calico'].includes(c.hair)) {
    const isCat = c.hair === 'calico';
    path('M119 116 Q105 158 107 187 L98 201 L119 203 L113 217 L146 212 L153 221 L249 219 L256 211 L284 215 L278 201 L299 202 Q277 180 282 124Z', isCat ? '#FFF3DF' : '#363039');
    if (isCat) {
      path('M113 128 L134 143 L142 189 L129 211 L113 217 L119 203 L98 201 Q108 185 106 171Z', '#EAA052', 'none');
      path('M269 161 L283 161 Q283 187 299 202 L278 201 L284 215 L260 211Z', '#343337', 'none');
    } else line('M122 150 Q112 183 127 199 M279 154 Q276 183 283 192', '#635760', 3);
  }
}

function footwear(c, skirt = false) {
  layer = 'legs';
  if (skirt) {
    path('M163 304 L163 361 Q169 370 182 364 L189 305 M212 305 L218 364 Q231 370 237 361 L237 303', skin, skinEdge);
    const shoes = c.outfit === 'blackDress' ? '#F1BD40' : c.outfit === 'redBowDress' ? '#333238' : c.outfit === 'sailor' ? '#3B3B45' : '#FFF8EB';
    if (c.outfit !== 'blackDress') {
      path('M162 343 L184 343 L181 366 L163 366Z M216 343 L238 343 L237 366 L219 366Z', '#FFF9ED', '#C7BAAD', 1.8);
      line('M167 347 L166 356 M224 347 L225 356', '#DDD5C9', 1.4);
    }
    path('M163 360 Q171 365 181 360 L185 373 Q184 380 153 379 Q147 372 163 360Z M219 360 Q229 365 237 360 Q253 372 247 379 L217 379 Q212 373 219 360Z', shoes, c.outfit === 'blackDress' ? '#BD8B36' : ink, 2.5);
    line('M154 375 L181 375 M219 375 L246 375', '#FAE8CF', 2.2);
    if (c.outfit === 'redBowDress' || c.outfit === 'blackDress') line('M162 366 L181 366 M219 366 L238 366', '#EBC983', 3);
  } else {
    const pants = ['orangeTee', 'stripeTee'].includes(c.outfit) ? '#85845A' : c.outfit === 'jacket' ? '#557A9A' : c.outfit === 'navySuit' ? '#354D63' : '#3E3E46';
    path('M157 277 L243 277 L241 359 Q229 365 212 360 L201 310 L192 360 Q174 366 159 358Z', pants);
    path('M197 297 L201 310 L192 360 L180 362 L189 309Z M232 290 L241 285 L241 359 L231 362Z', c.outfit === 'jacket' ? '#3D607D' : '#343B36', 'none', 0, 0.32);
    line('M174 318 L171 350 M225 322 L230 349', c.outfit === 'jacket' ? '#96B3C7' : '#B8BAA0', 1.7);
    if (['orangeTee', 'stripeTee'].includes(c.outfit)) {
      path('M158 291 L174 285 L181 295 L170 306 L158 303Z M211 300 L224 292 L237 299 L238 312 L225 309 L218 317Z M166 326 L180 317 L189 327 L184 341 L171 337 L160 344Z M218 340 L229 329 L239 335 L240 350 L227 347 L221 357Z', '#515D40', 'none');
      path('M178 288 L189 295 L184 306 L177 301Z M162 311 L175 310 L170 319 L160 320Z M224 319 L238 320 L232 329Z M177 347 L189 342 L190 357 L177 358Z', '#C1AF73', 'none');
      path('M158 302 L173 302 L173 319 L158 317Z M227 312 L241 311 L241 328 L228 329Z', '#949465', '#535C41', 1.5);
    }
    const shoes = ['orangeTee', 'stripeTee'].includes(c.outfit) ? c.accent : c.outfit === 'jacket' ? '#F0EBE2' : '#303039';
    path('M159 355 Q174 360 191 355 L191 375 Q174 382 145 379 L145 369Z M212 355 Q227 360 241 355 L255 369 L255 379 Q225 382 211 375Z', shoes);
    path('M145 372 Q166 376 191 369 L191 377 Q166 384 145 379Z M211 369 Q232 376 255 372 L255 379 Q231 384 211 377Z', c.outfit.includes('Suit') || c.outfit === 'suit' ? '#55565E' : '#FFF8E9', 'none');
    line('M162 363 L180 365 M159 368 L178 370 M221 365 L239 363 M223 370 L241 368', '#FDF3E3', 2);
  }
}

function arms(c, mood, fill, edge = ink) {
  layer = 'arms';
  const bare = ['blackDress', 'creamDress', 'redBowDress', 'orangeTee', 'stripeTee'].includes(c.outfit);
  if (mood === 'sleep') {
    path('M157 219 Q140 219 134 241 L134 259 Q136 273 155 280 L169 275 L168 264 L153 256 L164 239Z', bare ? skin : fill, bare ? skinEdge : edge);
    path('M243 219 Q260 219 266 241 L266 259 Q264 273 245 280 L231 275 L232 264 L247 256 L236 239Z', bare ? skin : fill, bare ? skinEdge : edge);
  } else if (mood === 'happy') {
    path('M157 219 Q141 218 134 238 L120 273 Q119 283 129 286 Q139 289 144 278 L166 240Z', bare ? skin : fill, bare ? skinEdge : edge);
    path('M242 219 Q257 216 268 199 L280 181 Q286 177 292 183 Q298 189 290 200 L272 236 Q262 250 248 241Z', bare ? skin : fill, bare ? skinEdge : edge);
    path('M278 192 L276 180 Q272 171 276 168 Q280 166 285 176 L283 156 Q284 148 289 151 L293 171 L295 150 Q299 144 302 150 L301 172 L308 155 Q313 153 313 160 L307 180 Q304 197 292 200Z', skin, skinEdge, 2.1);
    line('M286 187 Q290 180 299 182', skinEdge, 1.6);
    if (!bare) path('M121 274 L116 287 Q116 298 124 298 Q133 303 138 290 L142 282Z', skin, skinEdge, 2);
  } else {
    path('M156 219 Q137 219 131 242 L128 287 Q136 298 149 289 L164 240Z', bare ? skin : fill, bare ? skinEdge : edge);
    path('M244 219 Q263 219 269 242 L272 287 Q264 298 251 289 L236 240Z', bare ? skin : fill, bare ? skinEdge : edge);
    if (!bare) {
      path('M129 285 L128 299 Q130 309 137 308 Q145 309 147 300 L149 286 M251 286 L253 300 Q255 309 263 308 Q270 309 272 299 L271 285', skin, skinEdge, 2);
      line('M136 298 L137 303 M264 298 L263 303', skinEdge, 1.4);
    }
  }
  if (c.outfit === 'redBowDress') {
    path('M156 218 Q146 211 137 225 Q131 234 137 244 L153 246 L166 231Z', fill);
    path(mood === 'happy' ? 'M242 218 Q250 210 258 210 L274 225 Q268 242 255 243 L238 230Z' : 'M244 218 Q254 211 263 225 Q269 234 263 244 L247 246 L234 231Z', fill);
  }
}

function outfit(c, mood) {
  const isSkirt = ['blackDress', 'creamDress', 'hoodDress', 'redBowDress', 'sailor', 'hoodie'].includes(c.outfit);
  footwear(c, isSkirt);
  const fills = { suit: '#414149', navySuit: '#354D63', jacket: '#E8D8B7', blackDress: '#36343B', creamDress: '#FFF0D7', hoodDress: '#FFF2DE', hoodie: '#FAF4E7', redBowDress: '#39353A', sailor: '#B9D6ED', orangeTee: '#FF9037', stripeTee: '#FF9037' };
  const fill = fills[c.outfit];
  arms(c, mood, fill);
  layer = 'clothing';
  const neckline = 'M157 218 Q180 208 200 211 Q221 208 243 218';
  if (['suit', 'navySuit', 'jacket'].includes(c.outfit)) {
    path(`${neckline} L251 289 Q231 300 202 289 Q177 300 149 289Z`, fill);
    if (c.outfit === 'jacket') {
      path('M182 216 L218 216 L224 290 L178 290Z', '#35353A');
      path('M164 216 L179 210 L187 224 L171 238 L159 225Z M236 216 L221 210 L213 224 L229 238 L241 225Z', '#40546C');
      line('M180 233 L175 285 M220 233 L225 285', '#FDF3DC', 3);
      line('M156 267 L166 255 M244 267 L234 255', '#40546C', 4);
      ellipse(169, 223, 2.5, 2.5, '#D7B468'); ellipse(231, 223, 2.5, 2.5, '#D7B468');
    } else {
      path('M179 213 L200 262 L221 213Z', '#FFF7E8');
      path('M195 223 L205 223 L210 248 L200 266 L190 248Z', c.outfit === 'suit' ? c.accent : '#33353C');
      path('M190 215 L210 215 L205 226 L195 226Z', c.outfit === 'suit' ? '#E47829' : '#33353C', ink, 1.5);
      if (c.outfit === 'suit') line('M194 236 L205 231 M195 248 L208 242', '#FFDB87', 2.4);
      path('M175 212 L183 222 L200 266 L178 250 L181 240 L168 232Z M225 212 L217 222 L200 266 L222 250 L219 240 L232 232Z', c.outfit === 'suit' ? '#52525B' : '#456078');
      line('M158 273 L178 275 M222 275 L242 273', '#1F2933', 2);
      ellipse(201, 274, 2.5, 2.5, '#C6B596');
      line('M151 282 Q165 288 187 286', c.outfit === 'suit' ? '#696670' : '#64809A', 1.7);
    }
  } else if (['orangeTee', 'stripeTee'].includes(c.outfit)) {
    path('M156 218 L142 222 L130 246 L147 255 L153 246 L152 282 Q198 300 247 282 L246 246 L253 254 L270 246 L258 222 L242 217 Q201 207 156 218Z', fill, '#A7542D');
    path('M153 246 Q159 264 157 281 Q202 295 246 281 L243 272 Q201 286 168 274 L166 243Z', '#EF7427', 'none');
    path('M182 215 Q200 231 219 215 L217 223 Q201 239 184 224Z', '#E76F28', '#A7542D', 1.7);
    line('M139 245 L146 248 M255 248 L263 244 M161 278 Q201 290 238 279', '#FFD184', 2.1);
    if (c.outfit === 'stripeTee') path('M172 242 L230 242 L230 255 L172 255Z', '#FFF4D9', 'none');
    else line('M168 258 Q179 249 181 238 M217 270 Q231 266 236 256', '#F8BB69', 2);
  } else if (c.outfit === 'sailor') {
    path('M163 277 L237 277 L256 320 Q200 338 144 320Z', '#354461');
    path('M165 284 L173 322 M182 287 L185 326 M200 289 L200 328 M218 287 L215 326 M235 284 L227 322', 'none', '#65768F', 2);
    path(`${neckline} L245 280 Q201 291 155 280Z`, fill, '#48586D');
    path('M171 214 L200 241 L230 214 L223 212 L200 232 L178 212Z', '#354461');
    line('M179 219 L200 237 L222 220', '#F2F6EC', 1.7);
    path('M200 240 Q175 238 181 249 L199 245 Q183 259 191 274 L201 255 Q205 269 214 274 Q221 255 203 245 L219 250 Q231 237 202 240Z', '#354461', '#29364D', 1.8);
    line('M157 277 Q199 284 243 277', '#EBF2EE', 2.2);
  } else if (['hoodie', 'hoodDress'].includes(c.outfit)) {
    const long = c.outfit === 'hoodDress';
    path(long ? `${neckline} L254 326 Q200 342 146 326Z` : `${neckline} L249 308 Q201 319 151 308Z`, fill, '#AFA290');
    if (long) {
      path('M155 244 Q170 237 173 251 Q175 270 156 280 L152 269Z M224 215 Q230 227 244 232 L243 218Z', '#E5A054', 'none');
      path('M151 298 Q167 291 173 305 Q178 320 158 330 L147 326Z M229 267 Q245 258 248 281 L248 297 Q229 305 224 289Z', '#EDAC61', 'none');
      path('M154 222 Q166 216 172 228 Q175 238 156 242Z M181 273 Q194 269 195 285 Q195 299 181 301 Q171 288 181 273Z', '#474044', 'none');
    } else {
      path('M170 270 Q200 278 230 270 L235 293 Q200 306 165 293Z', '#EAE3D6', '#BEB3A3', 1.7);
      line('M170 278 L166 290 M230 278 L234 290 M201 230 L201 305', '#C1B6A6', 1.6);
      ellipse(201, 249, 3, 4, cream, '#B7AA99', 1.4);
    }
    path('M167 211 Q168 229 196 235 L186 215 M233 211 Q232 229 204 235 L214 215', fill, '#BEB3A3', 2);
    line('M182 230 L180 253 M218 230 L220 253', '#A79A88', 2.5);
    ellipse(180, 254, 2, 4, '#CFBFA7'); ellipse(220, 254, 2, 4, '#CFBFA7');
    line(long ? 'M154 323 Q200 335 246 323' : 'M156 305 Q200 314 244 305', '#D9CCB8', 2);
  } else {
    path(`${neckline} Q239 247 242 270 L263 319 Q201 344 137 319 L160 269Z`, fill);
    path('M177 270 L162 317 M200 278 L200 326 M224 270 L241 317', 'none', c.outfit === 'creamDress' ? '#D8C8AE' : '#60545E', 2.2);
    if (c.outfit === 'creamDress') {
      path('M171 215 Q176 235 200 237 Q223 235 229 215', 'none', '#DBC9AC', 2);
      line('M157 319 Q200 331 244 319', '#E9D9BE', 2.6);
    } else {
      path('M179 212 L200 216 Q189 241 174 224Z M221 212 L200 216 Q211 241 226 224Z', '#FFF5E5', ink, 1.6);
      if (c.outfit === 'redBowDress') {
        bow(200, 234, '#D64B43', '#853D3B');
        path('M194 240 L187 259 L198 254 L202 243 L211 258 L217 256 L207 240Z', '#D64B43', '#853D3B', 1.6);
        path('M157 269 Q200 279 243 269 L245 281 Q200 291 155 281Z', '#292B32');
        path('M193 273 L208 273 L208 286 L193 286Z M199 278 L204 278 L204 282 L199 282Z', '#DDB765', '#AD8744', 1.5);
        ellipse(200, 258, 2.5, 2.5, '#DDB765');
      }
    }
  }
  if (mood === 'sleep') {
    layer = 'resting-hands';
    path('M152 265 Q163 257 183 272 L200 276 Q207 282 200 287 Q190 290 181 283 L158 280Z M248 265 Q237 257 217 272 L200 276 Q193 282 200 287 Q210 290 219 283 L242 280Z', skin, skinEdge, 2);
    line('M180 275 L189 280 M220 275 L211 280', skinEdge, 1.5);
  }
}

function head(c) {
  layer = 'face';
  path('M184 198 L183 220 Q200 231 217 220 L216 198Z', skin, skinEdge, 2.1);
  path('M184 201 Q200 213 216 201 L216 212 Q200 224 184 212Z', '#E8AA87', 'none');
  ellipse(124, 154, 16, 22, skin, skinEdge, 2.5); ellipse(276, 154, 16, 22, skin, skinEdge, 2.5);
  path('M121 145 Q110 147 119 162 M279 145 Q290 147 281 162', 'none', '#DAA084', 2.8);
  path('M126 113 C123 83 154 65 200 65 C244 65 276 84 274 117 L274 163 C274 193 243 212 200 213 C157 212 126 193 126 163Z', skin, skinEdge, 2.8);
  path('M131 160 Q136 192 173 201 Q217 215 254 189 Q236 210 200 213 C157 212 126 193 126 163Z', '#EDB18F', 'none', 0, 0.55);
  ellipse(153, 175, 17, 8, '#F18F8B', 'none', 0, 0.47); ellipse(247, 175, 17, 8, '#F18F8B', 'none', 0, 0.47);
  line('M146 174 L144 178 M153 175 L151 179 M247 175 L245 179 M254 174 L252 178', '#E58F88', 1.3);
  path('M199 161 Q194 170 203 170', 'none', '#CF9374', 1.8);
  ellipse(198, 166, 2.5, 1.8, '#FFF0D8');
}

function eyes(c, mood) {
  layer = 'expression';
  const closed = mood === 'sleep' || mood === 'happy' || c.smileEyes;
  for (const x of [165, 235]) {
    if (closed) {
      line(mood === 'sleep' ? `M${x - 15} 150 Q${x} 163 ${x + 15} 150` : `M${x - 15} 152 Q${x} 134 ${x + 15} 152`, '#423336', 4.1);
      if (c.lashes) line(`M${x - 15} 150 L${x - 20} 146`, '#423336', 2.4);
    } else {
      path(`M${x - 19} 145 Q${x - 16} 130 ${x} 132 Q${x + 18} 131 ${x + 19} 145 Q${x + 18} 165 ${x} 168 Q${x - 18} 165 ${x - 19} 145Z`, '#FFF9EA', '#62483D', 2);
      ellipse(x, 150, 12.5, 16, c.iris, '#4D4039', 1.5);
      ellipse(x, 148, 6.7, 11, '#2E3035');
      path(`M${x - 9} 158 Q${x} 166 ${x + 9} 158 Q${x} 171 ${x - 9} 158Z`, '#D6C584', 'none');
      ellipse(x - 4, 141, 5, 5, '#FFFFFF'); ellipse(x + 5, 156, 2, 2, '#FFF6E8');
      line(`M${x - 19} 145 Q${x - 14} 131 ${x} 132 Q${x + 14} 130 ${x + 19} 144`, '#3E3032', c.lashes ? 3.5 : 2.8);
      if (c.lashes) line(`M${x - 17} 139 L${x - 23} 135 M${x + 16} 139 L${x + 21} 135`, '#3E3032', 2.4);
    }
    line(`M${x - 14} 122 Q${x} ${mood === 'sleep' ? 117 : 114} ${x + 12} 121`, c.hair === 'redTails' ? '#AA4939' : '#665045', c.lashes ? 2.5 : 4.1);
  }
  if (mood === 'sleep') {
    ellipse(200, 186, 4, 5, '#B87365', '#995D53', 1.5);
  } else if (mood === 'happy' || c.smileEyes) {
    path('M183 180 Q200 185 217 180 Q215 200 201 201 Q186 200 183 180Z', '#974A46', '#864740', 2.2);
    path('M185 182 Q200 187 215 182 L212 189 Q200 193 188 189Z', '#FFF9EB', 'none');
    path('M191 197 Q200 189 211 196 Q202 204 191 197Z', '#E58F83', 'none');
  } else {
    line('M190 184 Q200 191 212 183', '#A05D52', 2.1);
  }
  if (c.beard) {
    path('M181 176 Q190 171 200 176 Q210 171 220 176 Q212 182 200 179 Q188 182 181 176Z', '#675042', 'none');
    line('M194 200 Q200 203 207 199', '#977059', 3);
  }
}

function hair(c) {
  layer = 'hair-front';
  switch (c.hair) {
    case 'swept':
      path('M125 147 Q111 130 115 102 L106 99 L118 85 L111 80 Q119 63 144 51 L136 47 Q172 24 204 35 Q243 20 258 49 Q286 55 285 94 Q285 124 274 145 L267 110 Q260 83 240 80 C201 74 171 107 142 101 L131 125Z', '#343236');
      path('M126 88 Q155 40 208 44 Q228 41 241 55 Q205 44 170 71 L145 93Z', '#565057', 'none');
      path('M151 96 Q191 56 229 61 Q197 62 173 88Z', '#726169', 'none');
      line('M130 74 Q161 43 190 44 M143 85 Q186 44 230 53 M250 61 Q275 70 274 110', '#8C7778', 2.2);
      break;
    case 'spiky':
    case 'tousled':
    case 'streaked': {
      const streaked = c.hair === 'streaked';
      path('M122 148 L114 123 L119 101 L106 107 L119 84 L109 82 L135 68 L127 57 L155 57 L151 45 L177 49 L174 36 L199 44 L218 31 L220 45 L244 41 L238 53 L267 57 L259 65 L284 79 L272 85 L290 105 L278 109 L281 134 L272 148 L264 111 L246 98 L247 116 L226 96 L211 120 L207 97 L180 116 L177 99 L150 117 L148 99 L132 121Z', '#373137');
      path('M127 87 Q168 51 204 54 L184 69 L195 64 L173 85 L155 88 L165 74 L142 88Z M212 51 L230 51 L254 67 L242 64 L263 86 L235 75Z', streaked ? '#DBC9B0' : '#6F5647', 'none');
      if (streaked) path('M238 76 L250 86 L253 101 L243 94 L248 113 L232 103 L223 85Z M164 96 L150 110 L148 99 L136 116 L151 90Z', '#F3E1C5', 'none');
      line('M138 76 L160 64 M186 52 L177 62 M224 56 L239 66', '#AB8663', 2);
      break;
    }
    case 'parted':
      path('M123 147 Q111 124 114 99 Q116 68 146 54 Q188 25 219 43 Q251 39 267 64 Q288 84 280 125 L272 146 L263 109 Q244 106 225 74 Q209 101 189 107 L190 96 Q162 120 140 113 L130 139Z', '#303139');
      path('M125 104 Q135 62 177 55 Q199 42 215 51 C189 55 185 82 148 101Z', '#50505B', 'none');
      line('M135 86 Q159 57 189 58 M156 97 Q190 78 198 62 M233 58 Q249 71 251 88', '#77717A', 2.3);
      break;
    case 'bob':
      path('M118 162 Q102 135 113 96 Q122 44 174 41 Q200 33 232 43 Q279 55 288 107 Q295 147 279 169 L264 136 L261 105 Q217 117 156 105 L148 144 L137 174 L127 182Z', '#3D353E');
      path('M129 99 Q129 64 168 54 Q212 40 244 58 Q213 52 188 67 L179 102Z', '#60515A', 'none');
      path('M161 66 L158 104 M179 59 L178 106 M198 57 L198 108 M218 59 L219 107 M237 68 L240 106', 'none', '#81707A', 1.6);
      line('M134 110 L126 151 M270 111 L279 146', '#6C5C66', 2.2);
      path('M120 109 L143 122 L138 130 L114 117Z', '#777780', '#3F3D48', 1.7);
      line('M117 106 L138 127 M125 101 L146 121', '#B0A3AC', 2);
      break;
    case 'whiteHood':
    case 'calicoHood': {
      const calico = c.hair === 'calicoHood';
      path('M117 90 Q102 57 110 26 Q136 22 160 49 Q200 35 240 49 Q265 22 290 26 Q298 57 283 90 L294 147 Q306 192 270 212 Q240 223 218 213 L264 181 L274 114 Q272 68 201 63 Q128 68 126 114 L136 181 L182 213 Q154 223 130 212 Q94 192 106 149Z', '#FAF3E5', '#B2A493', 2.8);
      path('M117 38 Q135 39 147 53 L124 76Z M283 38 Q265 39 253 53 L276 76Z', '#E9A69B', 'none');
      path('M124 85 Q149 61 178 67 Q153 95 164 126 L147 153 L144 180 L165 194 Q140 197 135 178 L125 136Z', calico ? '#CFC0AA' : '#E6E0D7', '#A99D8C', 2);
      path('M275 86 L275 143 L265 181 Q260 197 236 195 L253 177 L253 128 L227 100 L222 68Z', '#E3D9C9', '#A99D8C', 2);
      path('M136 105 Q143 71 179 68 L200 65 Q190 96 161 119 L143 123Z', '#36343A');
      path('M198 65 Q225 61 256 78 Q259 103 263 119 Q235 111 219 84 Q214 110 184 119 Q193 94 198 65Z', '#FFF9EC', '#B1A292', 1.8);
      if (calico) {
        path('M218 65 Q244 65 256 78 L263 119 Q241 112 226 92Z M111 66 L117 89 L132 72 L139 64 L126 55Z M257 48 L274 42 L287 59 L282 76Z', '#E9A04F', 'none');
        path('M147 47 L160 49 L179 43 Q166 58 153 62Z', '#444047', 'none');
      }
      line('M115 128 Q103 181 133 200 M286 132 Q299 181 267 201', '#FFFDF1', 4);
      break;
    }
    case 'calico':
      path('M119 83 Q103 51 115 29 Q138 26 162 49 L238 49 Q263 26 285 29 Q297 51 281 83Z', '#FFF2DE');
      path('M119 40 Q141 42 150 55 L126 73Z M281 40 Q259 42 250 55 L274 73Z', '#E39D8D', 'none');
      path('M110 117 Q100 93 126 66 Q159 43 200 47 Q244 42 274 72 L288 111 L300 123 L279 125 L285 145 L263 137 L252 105 L240 99 L245 117 L220 99 L207 119 L197 101 L175 117 L176 103 L151 124 L140 116 L126 142 L122 124 L103 136Z', '#FFF1DC', '#A99989');
      path('M131 64 Q158 47 185 48 L197 62 Q181 75 181 97 L151 124 L140 116 L129 132 L136 101 L113 122 L110 117 Q100 93 131 64Z', '#37353A', 'none');
      path('M207 47 Q249 45 274 72 L288 111 L300 123 L279 125 L285 145 L263 137 L252 105 L240 99 L245 117 L225 104 L230 85 L216 71Z', '#EEA053', 'none');
      path('M167 57 L178 52 L183 66 L170 91 L151 107Z', '#6D6060', 'none');
      line('M191 69 Q189 91 182 103 M241 72 L261 98', '#FFF9E9', 3);
      break;
    case 'redTails':
      path('M129 86 Q108 53 121 24 Q148 23 172 51 L228 51 Q253 24 279 24 Q292 53 271 86Z', '#D74741', '#823E3B');
      path('M128 36 Q149 40 156 54 L135 70Z M272 36 Q251 40 244 54 L265 70Z', '#FFC7A0', 'none');
      path('M123 152 Q106 127 115 94 Q126 51 172 52 Q200 43 230 53 Q276 59 285 104 Q292 136 277 161 L264 143 L257 105 L247 95 L250 120 L228 110 L220 95 Q210 122 188 129 L194 109 Q169 130 146 127 L138 155Z', '#DF4C45', '#863E3B');
      path('M126 115 Q126 77 163 65 Q184 56 200 57 Q166 75 160 102 L150 117Z M205 57 Q233 53 253 77 L256 96 Q229 69 205 57Z', '#F67660', 'none');
      line('M140 93 Q155 70 177 68 M220 64 Q239 67 248 79', '#FFBA84', 2.5);
      bow(118, 105, '#39333B'); bow(282, 105, '#39333B');
      break;
    case 'long':
      path('M119 156 Q106 127 115 94 Q126 47 166 44 Q201 27 235 43 Q278 57 282 96 L279 162 L266 175 L265 121 Q243 105 229 75 Q215 107 187 121 L191 107 Q158 139 135 130 L132 166Z', '#34333B');
      path('M125 119 Q129 71 169 58 Q195 45 217 47 Q185 58 176 85 Q158 108 125 119Z', '#56505A', 'none');
      path('M238 60 Q264 75 269 110 L257 103Z', '#645761', 'none');
      line('M140 96 Q159 68 185 59 M153 116 Q187 96 195 70 M247 73 L258 93', '#8B7480', 2);
      break;
  }
}

function glasses(c) {
  if (!c.glasses) return;
  layer = 'glasses';
  const color = c.glasses;
  path('M134 139 Q154 131 188 138 L184 164 Q169 175 144 167Z M212 138 Q246 131 266 139 L256 167 Q231 175 216 164Z', 'none', color, c.id === 'fengge' ? 5 : 3.5);
  line('M188 142 Q200 135 212 142 M133 141 L124 137 M266 141 L276 137', color, 3.5);
  line('M141 143 L147 140 M221 142 L228 140', '#FFEDDA', 1.6);
}

function fortune(c, mood) {
  layer = 'cushion';
  path('M90 342 Q94 322 200 321 Q307 322 311 342 L313 357 Q300 381 200 383 Q100 381 87 357Z', '#C84641', '#853D3B');
  path('M94 350 Q200 373 306 350 L309 359 Q201 389 91 359Z', '#AA3438', 'none');
  line('M103 341 Q200 323 297 341 M101 358 Q200 379 298 358', '#EFAC63', 2.2);
  for (const x of [128, 174, 223, 270]) path(`M${x} 349 L${x + 9} 357 L${x} 365 L${x - 9} 357Z`, 'none', '#DF9551', 1.7);
  layer = 'clothing';
  path('M147 205 Q116 214 112 261 L118 331 Q141 362 200 355 Q261 362 282 331 L288 263 Q282 218 250 205Z', '#FFF3E0', '#A68A71');
  path('M120 301 Q145 283 157 308 L155 343 L132 342Z M244 311 Q257 286 280 308 L277 331 L261 346 L247 341Z', '#B19075', 'none');
  path('M137 213 Q161 237 186 230 L180 205 M263 213 Q239 237 214 230 L220 205', '#FFF9EC', '#D5BFA3', 2);
  layer = 'head';
  ellipse(119, 153, 17, 23, skin, skinEdge); ellipse(281, 153, 17, 23, skin, skinEdge);
  path('M123 112 Q124 67 200 66 Q276 67 277 112 L280 166 Q282 213 200 220 Q118 213 120 166Z', skin, skinEdge);
  ellipse(146, 172, 24, 13, '#F2958B', 'none', 0, 0.6); ellipse(254, 172, 24, 13, '#F2958B', 'none', 0, 0.6);
  layer = 'hair';
  path('M122 145 Q104 124 118 103 Q106 80 130 71 Q125 50 151 50 Q153 28 179 41 Q198 18 219 38 Q248 25 259 49 Q287 48 283 73 Q301 90 282 111 L279 145 L269 119 Q257 100 248 94 Q200 106 151 93 L130 118Z', '#F9EBD1', '#A98A70');
  path('M215 39 Q241 28 250 50 Q262 64 247 93 L228 98 Q238 66 215 39Z M128 101 L141 90 Q149 112 124 137Z', '#AC7B59', 'none');
  path('M161 49 Q147 70 172 84 M183 42 Q169 70 193 88 M204 40 Q193 67 213 88 M261 60 Q277 80 260 95', 'none', '#D6BB96', 3);
  layer = 'expression';
  line('M143 127 Q156 119 171 125 M229 125 Q244 119 257 127', '#AB7856', 6);
  if (mood === 'sleep') {
    line('M141 151 Q158 164 176 151 M224 151 Q242 164 259 151', ink, 3.7);
    ellipse(200, 190, 5, 6, '#A96356');
  } else {
    line('M140 154 Q157 135 176 153 M224 153 Q243 135 260 154', ink, 4);
    path(mood === 'happy' ? 'M176 182 Q200 191 224 182 Q220 207 200 208 Q180 207 176 182Z' : 'M179 186 Q200 202 224 184', mood === 'happy' ? '#A8554C' : 'none', '#A06450', 2.3);
    if (mood === 'happy') path('M181 185 Q200 193 219 185 L215 193 Q200 199 185 193Z', '#FFF8E8', 'none');
  }
  ellipse(200, 173, 9, 5, '#D98573');
  layer = 'bow';
  bow(200, 227, '#D84E43', '#A34B40');
  line('M180 215 L184 231 M217 216 L213 232', '#FFF4DD', 5);
  ellipse(200, 235, 9, 10, '#F0C34E', '#A67932', 1.8);
  ellipse(200, 237, 2.8, 3.3, '#72502D'); line('M200 239 L200 244', '#72502D', 1.6);
  layer = 'ingot';
  path('M138 267 Q166 252 200 263 Q234 252 262 267 L248 290 L240 318 Q200 333 160 318 L152 290Z', '#EAB437', '#9E722D');
  path('M138 267 Q167 282 200 280 Q237 282 262 267 L252 281 Q200 304 148 281Z', '#FFE191', '#B4832F', 1.8);
  path('M167 278 Q168 251 200 249 Q232 251 233 278 Q200 290 167 278Z', '#F7CB50', '#B4832F', 1.8);
  path('M178 266 Q195 252 212 259', 'none', '#FFF2B1', 5);
  path('M160 294 L166 315 Q203 328 237 316', 'none', '#CD9227', 3);
  layer = 'hands';
  path('M133 257 Q149 253 157 267 L156 286 Q156 299 144 299 Q126 300 122 282 Q119 267 133 257Z M267 257 Q251 253 243 267 L244 286 Q244 299 256 299 Q274 300 278 282 Q281 267 267 257Z', skin, skinEdge, 2.3);
  line('M143 269 L153 274 M143 281 L154 285 M257 269 L247 274 M257 281 L246 285', skinEdge, 1.6);
  layer = 'feet';
  path('M147 317 Q155 310 163 318 Q179 311 184 330 L187 349 Q179 369 158 361 Q143 358 137 339 Q132 326 140 322Z M253 317 Q245 310 237 318 Q221 311 216 330 L213 349 Q221 369 242 361 Q257 358 263 339 Q268 326 260 322Z', skin, skinEdge, 2.4);
  line('M148 324 L152 335 M158 321 L162 331 M252 324 L248 335 M242 321 L238 331', '#D7997A', 2);
}

function moodAccents(c, mood) {
  layer = 'mood';
  if (mood === 'happy') {
    path('M83 94 Q84 84 91 86 Q99 76 104 84 Q111 94 96 106Z', '#EF9C8C', 'none');
    path('M309 69 L313 57 L317 69 L328 73 L317 77 L313 89 L309 77 L298 73Z', '#EBC16D', 'none');
  } else if (mood === 'sleep') {
    line('M303 106 L318 106 L303 122 L318 122 M324 80 L343 80 L324 100 L343 100 M340 50 L365 50 L340 73 L365 73', '#8297AC', 3.6);
  }
}

await mkdir(destination, { recursive: true });
for (const c of roster) {
  for (const mood of moods) {
    paths = []; serial = 0;
    if (c.outfit === 'fortune') fortune(c, mood);
    else { rearHair(c); outfit(c, mood); head(c); eyes(c, mood); hair(c); glasses(c); }
    moodAccents(c, mood);
    const name = `${c.id}_human_${mood}`;
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" viewBox="0 0 400 400" role="img" aria-labelledby="title description">\n  <title id="title">${c.name}・${moodLabels[mood]}</title>\n  <desc id="description">原創 SVG 重繪，沿用現有角色的髮型、服裝與配色。透明背景、可縮放的 Q 版人物。</desc>\n${paths.join('\n')}\n</svg>\n`;
    await writeFile(join(destination, `${name}.svg`), svg);
  }
}
await writeFile(join(destination, 'roster.json'), JSON.stringify({ viewBox: [0, 0, 400, 400], moods, characters: roster.map(({ id, name }) => ({ id, name })) }, null, 2) + '\n');
console.log(`Drew ${roster.length * moods.length} SVG human character masters in art/characters.`);
