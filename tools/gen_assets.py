#!/usr/bin/env python3
"""Генерирует JSON-ресурсы и текстуры мода. Запуск из корня репозитория: python3 tools/gen_assets.py"""
import json, os, random
from PIL import Image, ImageDraw

RES = "src/main/resources"
A = f"{RES}/assets/vagonetki"
D = f"{RES}/data/vagonetki"
CL = f"{RES}/resourcepacks/classic/assets/vagonetki"
random.seed(7)

def wj(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write("\n")

RAILS = ["plane_rail", "boat_rail", "universal_rail"]
CARTS = ["plane_cart", "boat_cart", "slime_cart", "universal_cart"]

# ---------- блокстейты и модели ----------
for r in RAILS:
    for on in (False, True):
        s = "_on" if on else ""
        wj(f"{A}/models/block/{r}{s}.json", {"parent": "minecraft:block/rail_flat", "textures": {"rail": f"vagonetki:block/{r}{s}"}})
        wj(f"{A}/models/block/{r}{s}_raised_ne.json", {"parent": "minecraft:block/rail_raised_ne", "textures": {"rail": f"vagonetki:block/{r}{s}"}})
        wj(f"{A}/models/block/{r}{s}_raised_sw.json", {"parent": "minecraft:block/rail_raised_sw", "textures": {"rail": f"vagonetki:block/{r}{s}"}})
    v = {}
    for on in (False, True):
        s = "_on" if on else ""
        p = "true" if on else "false"
        m = lambda n: f"vagonetki:block/{r}{s}{n}"
        v[f"powered={p},shape=north_south"] = {"model": m("")}
        v[f"powered={p},shape=east_west"] = {"model": m(""), "y": 90}
        v[f"powered={p},shape=ascending_east"] = {"model": m("_raised_ne"), "y": 90}
        v[f"powered={p},shape=ascending_north"] = {"model": m("_raised_ne")}
        v[f"powered={p},shape=ascending_south"] = {"model": m("_raised_sw")}
        v[f"powered={p},shape=ascending_west"] = {"model": m("_raised_sw"), "y": 90}
    wj(f"{A}/blockstates/{r}.json", {"variants": v})
    wj(f"{A}/models/item/{r}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"vagonetki:block/{r}"}})
    wj(f"{D}/loot_tables/blocks/{r}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"vagonetki:{r}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
for c in CARTS:
    wj(f"{A}/models/item/{c}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"vagonetki:item/{c}"}})

wj(f"{RES}/data/minecraft/tags/blocks/rails.json", {"replace": False, "values": [f"vagonetki:{r}" for r in RAILS]})
wj(f"{RES}/data/minecraft/tags/blocks/mineable/pickaxe.json", {"replace": False, "values": [f"vagonetki:{r}" for r in RAILS]})

# ---------- рецепты ----------
def ing(x):
    return {"tag": x[4:]} if x.startswith("tag:") else {"item": x}
def shaped(name, pattern, key, result, count=1):
    wj(f"{D}/recipes/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern, "key": {k: ing(v) for k, v in key.items()}, "result": {"item": f"vagonetki:{result}", "count": count}})
def shapeless(name, items, result):
    wj(f"{D}/recipes/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [ing(i) for i in items], "result": {"item": f"vagonetki:{result}", "count": 1}})

shaped("plane_rail", ["IDI", "ISI", "I I"], {"I": "minecraft:iron_ingot", "D": "minecraft:black_dye", "S": "minecraft:stick"}, "plane_rail", 16)
shaped("boat_rail", ["IDI", "ISI", "I I"], {"I": "minecraft:iron_ingot", "D": "minecraft:green_dye", "S": "minecraft:stick"}, "boat_rail", 16)
shaped("universal_rail", ["GDG", "GSG", "GRG"], {"G": "minecraft:gold_ingot", "D": "minecraft:blue_dye", "S": "minecraft:stick", "R": "minecraft:redstone"}, "universal_rail", 8)
shaped("plane_cart", ["FIF", " M "], {"F": "minecraft:feather", "I": "minecraft:iron_ingot", "M": "minecraft:minecart"}, "plane_cart")
shapeless("boat_cart", ["minecraft:minecart", "tag:minecraft:boats"], "boat_cart")
shapeless("slime_cart", ["minecraft:minecart", "minecraft:slime_block"], "slime_cart")
shapeless("universal_cart", ["vagonetki:plane_cart", "vagonetki:boat_cart", "vagonetki:slime_cart"], "universal_cart")

# ---------- язык ----------
ru = {
    "itemGroup.vagonetki.main": "Вагонетки",
    "block.vagonetki.plane_rail": "Чёрный рельс (самолёт)",
    "block.vagonetki.boat_rail": "Зелёный рельс (лодка)",
    "block.vagonetki.universal_rail": "Синий рельс (универсальный)",
    "item.vagonetki.plane_cart": "Вагонетка-самолёт",
    "item.vagonetki.boat_cart": "Вагонетка-лодка",
    "item.vagonetki.slime_cart": "Слизь-вагонетка",
    "item.vagonetki.universal_cart": "Универсальная вагонетка",
    "entity.vagonetki.plane_cart": "Вагонетка-самолёт",
    "entity.vagonetki.boat_cart": "Вагонетка-лодка",
    "entity.vagonetki.slime_cart": "Слизь-вагонетка",
    "entity.vagonetki.universal_cart": "Универсальная вагонетка",
    "key.vagonetki.up": "Самолёт: набор высоты",
    "key.categories.vagonetki": "Вагонетки",
    "message.vagonetki.wrong_rail": "Эту вагонетку можно ставить только на рельсы своего цвета",
}
en = {
    "itemGroup.vagonetki.main": "Minecarts",
    "block.vagonetki.plane_rail": "Black Rail (Plane)",
    "block.vagonetki.boat_rail": "Green Rail (Boat)",
    "block.vagonetki.universal_rail": "Blue Rail (Universal)",
    "item.vagonetki.plane_cart": "Plane Minecart",
    "item.vagonetki.boat_cart": "Boat Minecart",
    "item.vagonetki.slime_cart": "Slime Minecart",
    "item.vagonetki.universal_cart": "Universal Minecart",
    "entity.vagonetki.plane_cart": "Plane Minecart",
    "entity.vagonetki.boat_cart": "Boat Minecart",
    "entity.vagonetki.slime_cart": "Slime Minecart",
    "entity.vagonetki.universal_cart": "Universal Minecart",
    "key.vagonetki.up": "Plane: climb",
    "key.categories.vagonetki": "Minecarts",
    "message.vagonetki.wrong_rail": "This minecart can only be placed on rails of its own colour",
}
wj(f"{A}/lang/ru_ru.json", ru)
wj(f"{A}/lang/en_us.json", en)

wj(f"{RES}/resourcepacks/classic/pack.mcmeta", {"pack": {"pack_format": 15, "description": "Vagonetki Classic: старый добрый плоский стиль"}})

# ---------- текстуры ----------
def px(img, x, y, c):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), c)

def shade(c, k):
    return tuple(max(0, min(255, int(v * k))) for v in c[:3]) + (255,)

def rail_tex(rail, sleeper, classic, lit=False):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    for y in (1, 5, 9, 13):
        d.rectangle([2, y, 13, y + 2], fill=sleeper if classic else shade(sleeper, 0.9))
        if not classic:
            d.line([2, y, 13, y], fill=shade(sleeper, 1.2))
    for x in (2, 12):
        d.rectangle([x, 0, x + 1, 15], fill=rail)
        if not classic:
            d.line([x, 0, x, 15], fill=shade(rail, 1.35))
            d.line([x + 1, 0, x + 1, 15], fill=shade(rail, 0.7))
    if lit:
        for y in range(0, 16, 4):
            px(im, 7, y, (255, 245, 150, 255)); px(im, 8, y, (255, 245, 150, 255))
    if classic:
        for x in range(16):
            for y in range(16):
                if im.getpixel((x, y))[3] and (x in (2, 13) or False):
                    pass
    return im

RAIL_COL = {"plane_rail": (28, 28, 32, 255), "boat_rail": (40, 170, 70, 255), "universal_rail": (50, 100, 230, 255)}
YELLOW = (255, 214, 40, 255)
SLEEPER = (110, 80, 45, 255)
SLEEPER_LIT = (150, 115, 60, 255)

def cart_icon(kind, classic):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    body = {"plane_cart": (170, 170, 178, 255), "boat_cart": (60, 140, 120, 255), "slime_cart": (110, 220, 90, 255), "universal_cart": (90, 110, 220, 255)}[kind]
    out = (20, 20, 20, 255)
    d.polygon([(2, 7), (13, 7), (12, 13), (3, 13)], fill=body, outline=out)
    d.rectangle([4, 14, 5, 15], fill=(60, 60, 60, 255)); d.rectangle([10, 14, 11, 15], fill=(60, 60, 60, 255))
    if not classic:
        d.line([3, 8, 12, 8], fill=shade(body, 1.25))
    if kind in ("plane_cart", "universal_cart"):
        d.rectangle([0, 5, 15, 6], fill=(235, 235, 235, 255), outline=(200, 40, 40, 255))
        d.rectangle([7, 1, 8, 4], fill=(70, 70, 70, 255))
        d.line([4, 2, 11, 2], fill=(120, 120, 120, 255))
    if kind in ("boat_cart", "universal_cart"):
        d.rectangle([1, 12, 14, 13], fill=(120, 85, 50, 255))
    if kind in ("slime_cart", "universal_cart"):
        for (x, y) in ((5, 9), (9, 10), (7, 11)):
            px(im, x, y, (190, 255, 170, 255))
    return im

def body_tex(kind, classic):
    col = {"plane_cart": (165, 168, 176, 255), "boat_cart": (55, 135, 115, 255), "slime_cart": (105, 215, 85, 255), "universal_cart": (85, 105, 215, 255)}[kind]
    im = Image.new("RGBA", (64, 32), col)
    d = ImageDraw.Draw(im)
    if not classic:
        for _ in range(180):
            x, y = random.randrange(64), random.randrange(32)
            px(im, x, y, shade(col, random.choice((0.85, 0.92, 1.1, 1.18))))
    d.rectangle([0, 0, 63, 31], outline=shade(col, 0.55 if classic else 0.7))
    return im

def extras_tex(classic):
    im = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    red, white = (200, 45, 45, 255), (238, 238, 238, 255)
    d.rectangle([0, 0, 140, 58], fill=white)
    for x in range(0, 141, 14):
        d.rectangle([x, 0, x + 3, 58], fill=red)
    d.rectangle([0, 60, 12, 71], fill=red)
    d.rectangle([0, 72, 40, 88], fill=red)
    d.rectangle([140, 0, 185, 18], fill=(60, 45, 35, 255))
    d.rectangle([150, 0, 185, 18], fill=(70, 70, 76, 255))
    d.rectangle([190, 0, 205, 8], fill=(150, 150, 158, 255))
    d.rectangle([0, 90, 72, 108], fill=(120, 85, 50, 255))
    d.rectangle([0, 90, 72, 92], fill=(40, 170, 70, 255))
    d.rectangle([0, 100, 72, 102], fill=(40, 170, 70, 255))
    if not classic:
        for _ in range(500):
            x, y = random.randrange(256), random.randrange(128)
            p = im.getpixel((x, y))
            if p[3]:
                px(im, x, y, shade(p, random.choice((0.92, 1.08))))
    return im

def save(im, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)

for classic, base in ((False, A), (True, CL)):
    for r in RAILS:
        c = RAIL_COL[r]
        save(rail_tex(c, SLEEPER, classic), f"{base}/textures/block/{r}.png")
        save(rail_tex(YELLOW, SLEEPER_LIT, classic, lit=True), f"{base}/textures/block/{r}_on.png")
    for k in CARTS:
        save(cart_icon(k, classic), f"{base}/textures/item/{k}.png")
        save(body_tex(k, classic), f"{base}/textures/entity/{k}.png")
    save(extras_tex(classic), f"{base}/textures/entity/cart_extras.png")
print("ok")
