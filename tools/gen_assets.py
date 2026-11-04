#!/usr/bin/env python3
"""Генерирует JSON-ресурсы и текстуры мода. Запуск из корня репозитория: python3 tools/gen_assets.py

Обычные текстуры детальные (градиенты, зерно, заклёпки, свечение). Текстуры Classic плоские, в старом стиле.
"""
import json, math, os, random
from PIL import Image, ImageDraw

RES = "src/main/resources"
A = f"{RES}/assets/vagonetki"
D = f"{RES}/data/vagonetki"
CL = f"{RES}/resourcepacks/classic/assets/vagonetki"

KINDS = ["plane", "boat", "slime", "universal", "helicopter", "submarine", "ice", "lava", "ender", "rocket"]
RAIL_KINDS = [k for k in KINDS if k != "slime"]
RAILS = [f"{k}_rail" for k in RAIL_KINDS]
CARTS = [f"{k}_cart" for k in KINDS]


def wj(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write("\n")


# ====================== JSON: модели, блокстейты, рецепты, язык ======================
for r in RAILS:
    for on in (False, True):
        s = "_on" if on else ""
        for suffix, parent in (("", "rail_flat"), ("_raised_ne", "rail_raised_ne"), ("_raised_sw", "rail_raised_sw")):
            wj(f"{A}/models/block/{r}{s}{suffix}.json", {"parent": f"minecraft:block/{parent}", "textures": {"rail": f"vagonetki:block/{r}{s}"}})
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


def ing(x):
    return {"tag": x[4:]} if x.startswith("tag:") else {"item": x}


def shaped(name, pattern, key, result, count=1):
    wj(f"{D}/recipes/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern, "key": {k: ing(v) for k, v in key.items()}, "result": {"item": f"vagonetki:{result}", "count": count}})


def shapeless(name, items, result):
    wj(f"{D}/recipes/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [ing(i) for i in items], "result": {"item": f"vagonetki:{result}", "count": 1}})


# Рельсы: железо, краситель, палка (16 штук). Универсальный рельс на золоте.
DYES = {
    "plane": "black_dye", "boat": "green_dye", "helicopter": "orange_dye", "submarine": "cyan_dye",
    "ice": "light_blue_dye", "lava": "red_dye", "ender": "purple_dye", "rocket": "white_dye",
}
for k, dye in DYES.items():
    shaped(f"{k}_rail", ["IDI", "ISI", "I I"], {"I": "minecraft:iron_ingot", "D": f"minecraft:{dye}", "S": "minecraft:stick"}, f"{k}_rail", 16)
shaped("universal_rail", ["GDG", "GSG", "GRG"], {"G": "minecraft:gold_ingot", "D": "minecraft:blue_dye", "S": "minecraft:stick", "R": "minecraft:redstone"}, "universal_rail", 8)

# Вагонетки
M = "minecraft:minecart"
shaped("plane_cart", ["FIF", " M "], {"F": "minecraft:feather", "I": "minecraft:iron_ingot", "M": M}, "plane_cart")
shapeless("boat_cart", [M, "tag:minecraft:boats"], "boat_cart")
shapeless("slime_cart", [M, "minecraft:slime_block"], "slime_cart")
shapeless("universal_cart", ["vagonetki:plane_cart", "vagonetki:boat_cart", "vagonetki:slime_cart"], "universal_cart")
shaped("helicopter_cart", ["III", " M "], {"I": "minecraft:iron_ingot", "M": M}, "helicopter_cart")
shapeless("submarine_cart", [M, "minecraft:prismarine_shard", "minecraft:prismarine_shard", "minecraft:glass"], "submarine_cart")
shapeless("ice_cart", [M, "minecraft:blue_ice"], "ice_cart")
shapeless("lava_cart", [M, "minecraft:magma_block", "minecraft:blaze_powder"], "lava_cart")
shapeless("ender_cart", [M, "minecraft:ender_eye"], "ender_cart")
shapeless("rocket_cart", [M, "minecraft:firework_rocket", "minecraft:firework_rocket", "minecraft:gunpowder"], "rocket_cart")

ru = {
    "itemGroup.vagonetki.main": "Вагонетки",
    "block.vagonetki.plane_rail": "Чёрный рельс (самолёт)",
    "block.vagonetki.boat_rail": "Зелёный рельс (лодка)",
    "block.vagonetki.universal_rail": "Синий рельс (универсальная)",
    "block.vagonetki.helicopter_rail": "Оранжевый рельс (вертолёт)",
    "block.vagonetki.submarine_rail": "Бирюзовый рельс (подлодка)",
    "block.vagonetki.ice_rail": "Ледяной рельс (ледяная)",
    "block.vagonetki.lava_rail": "Лавовый рельс (лавовая)",
    "block.vagonetki.ender_rail": "Фиолетовый рельс (эндер)",
    "block.vagonetki.rocket_rail": "Серебряный рельс (ракета)",
    "key.vagonetki.up": "Вагонетка: набор высоты / всплытие",
    "key.vagonetki.roll": "Самолёт: бочка",
    "key.vagonetki.loop": "Самолёт: петля",
    "key.categories.vagonetki": "Вагонетки",
    "message.vagonetki.wrong_rail": "Эту вагонетку можно ставить только на рельсы своего цвета",
}
en = {
    "itemGroup.vagonetki.main": "Minecarts",
    "block.vagonetki.plane_rail": "Black Rail (Plane)",
    "block.vagonetki.boat_rail": "Green Rail (Boat)",
    "block.vagonetki.universal_rail": "Blue Rail (Universal)",
    "block.vagonetki.helicopter_rail": "Orange Rail (Helicopter)",
    "block.vagonetki.submarine_rail": "Teal Rail (Submarine)",
    "block.vagonetki.ice_rail": "Ice Rail (Ice)",
    "block.vagonetki.lava_rail": "Lava Rail (Lava)",
    "block.vagonetki.ender_rail": "Purple Rail (Ender)",
    "block.vagonetki.rocket_rail": "Silver Rail (Rocket)",
    "key.vagonetki.up": "Minecart: climb / surface",
    "key.vagonetki.roll": "Plane: barrel roll",
    "key.vagonetki.loop": "Plane: loop",
    "key.categories.vagonetki": "Minecarts",
    "message.vagonetki.wrong_rail": "This minecart can only be placed on rails of its own colour",
}
NAMES = {
    "plane": ("Вагонетка-самолёт", "Plane Minecart"),
    "boat": ("Вагонетка-лодка", "Boat Minecart"),
    "slime": ("Слизь-вагонетка", "Slime Minecart"),
    "universal": ("Универсальная вагонетка", "Universal Minecart"),
    "helicopter": ("Вагонетка-вертолёт", "Helicopter Minecart"),
    "submarine": ("Вагонетка-подлодка", "Submarine Minecart"),
    "ice": ("Ледяная вагонетка", "Ice Minecart"),
    "lava": ("Лавовая вагонетка", "Lava Minecart"),
    "ender": ("Эндер-вагонетка", "Ender Minecart"),
    "rocket": ("Вагонетка-ракета", "Rocket Minecart"),
}
for k, (n_ru, n_en) in NAMES.items():
    for pre in ("item", "entity"):
        ru[f"{pre}.vagonetki.{k}_cart"] = n_ru
        en[f"{pre}.vagonetki.{k}_cart"] = n_en
wj(f"{A}/lang/ru_ru.json", ru)
wj(f"{A}/lang/en_us.json", en)
wj(f"{RES}/resourcepacks/classic/pack.mcmeta", {"pack": {"pack_format": 15, "description": "Vagonetki Classic: старый добрый плоский стиль"}})


# ====================== Текстуры ======================
def clamp(v):
    return max(0, min(255, int(v)))


def sh(c, k):
    return (clamp(c[0] * k), clamp(c[1] * k), clamp(c[2] * k), 255)


def mix(a, b, t):
    return tuple(clamp(a[i] * (1 - t) + b[i] * t) for i in range(3)) + (255,)


def px(im, x, y, c):
    if 0 <= x < im.width and 0 <= y < im.height:
        im.putpixel((x, y), c)


def grain(im, box, amt, rnd):
    """Лёгкое зерно по уже нарисованным пикселям области."""
    x0, y0, x1, y1 = box
    for x in range(x0, min(x1, im.width)):
        for y in range(y0, min(y1, im.height)):
            p = im.getpixel((x, y))
            if p[3]:
                k = 1.0 + rnd.uniform(-amt, amt)
                im.putpixel((x, y), (clamp(p[0] * k), clamp(p[1] * k), clamp(p[2] * k), p[3]))


def vgrad(im, box, top, bottom):
    x0, y0, x1, y1 = box
    h = max(1, y1 - y0 - 1)
    for y in range(y0, y1):
        c = mix(top, bottom, (y - y0) / h)
        for x in range(x0, x1):
            px(im, x, y, c)


def bevel(im, box, k=0.25):
    x0, y0, x1, y1 = box
    for x in range(x0, x1):
        for y in (y0, y1 - 1):
            p = im.getpixel((x, y))
            px(im, x, y, sh(p, 1 + k) if y == y0 else sh(p, 1 - k))
    for y in range(y0, y1):
        for x in (x0, x1 - 1):
            p = im.getpixel((x, y))
            px(im, x, y, sh(p, 1 + k * 0.6) if x == x0 else sh(p, 1 - k * 0.6))


def rivets(im, box, step, c):
    x0, y0, x1, y1 = box
    for x in range(x0 + 1, x1 - 1, step):
        for y in (y0 + 1, y1 - 2):
            px(im, x, y, c)


# ---------- рельсы ----------
THEME = {
    "plane": (28, 28, 34),
    "boat": (38, 176, 76),
    "universal": (48, 100, 236),
    "helicopter": (245, 132, 30),
    "submarine": (30, 192, 204),
    "ice": (160, 222, 250),
    "lava": (214, 48, 26),
    "ender": (146, 62, 214),
    "rocket": (214, 216, 224),
}
YELLOW = (255, 222, 56)


def rail_tex(kind, lit, classic):
    th = THEME[kind]
    rnd = random.Random(hash(kind) & 0xFFFF if False else sum(map(ord, kind)) + (7 if lit else 0))
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    wood = (124, 90, 54, 255)
    if lit:
        wood = mix(wood, (255, 205, 90), 0.18)
    for y0 in (0, 4, 8, 12):
        for y in range(y0, y0 + 3):
            for x in range(1, 15):
                k = 1.0
                if not classic:
                    k = 1.16 if y == y0 else (0.78 if y == y0 + 2 else 1.0)
                    k *= 1.0 + 0.06 * math.sin(x * 1.7 + y0) + rnd.uniform(-0.05, 0.05)
                elif y == y0 + 2:
                    k = 0.8
                px(im, x, y, sh(wood, k))
        if not classic:
            for x in (2, 13):  # гвозди
                px(im, x, y0 + 1, (60, 60, 68, 255))
            px(im, 2, y0, (150, 150, 160, 255))
            px(im, 13, y0, (150, 150, 160, 255))
        if lit:
            lamp = (255, 247, 170, 255) if not classic else YELLOW
            px(im, 7, y0 + 1, lamp)
            px(im, 8, y0 + 1, lamp)
            if not classic:
                halo = (255, 196, 70, 255)
                for x, y in ((6, y0 + 1), (9, y0 + 1), (7, y0), (8, y0), (7, y0 + 2), (8, y0 + 2)):
                    px(im, x, y, halo)
    base = YELLOW if lit else th
    for rx in (2, 12):
        for y in range(16):
            if classic:
                px(im, rx, y, sh(base, 1.0))
                px(im, rx + 1, y, sh(base, 0.78))
                continue
            spec = 1.0 + 0.10 * math.sin(y * 0.9 + (3 if rx == 12 else 0))
            if lit:
                px(im, rx, y, sh(base, 1.12 * spec))
                px(im, rx + 1, y, sh(base, 0.82 * spec))
            else:
                px(im, rx, y, sh(base, 1.35 * spec))
                px(im, rx + 1, y, sh(base, 0.72 * spec))
        # цветная кромка снаружи у включённого рельса, чтобы был виден цвет типа
        if lit:
            edge_x = rx if rx == 2 else rx + 1
            for y in range(16):
                px(im, edge_x, y, sh(th, 1.0 if not classic else 0.9))
    if not classic and not lit:
        for rx in (2, 12):  # блики и стыки
            for y in (3, 11):
                px(im, rx, y, sh(th, 0.5))
        if kind == "ice":
            for y in (1, 6, 10, 14):
                px(im, 2, y, (255, 255, 255, 255))
                px(im, 12, y, (255, 255, 255, 255))
        if kind == "lava":
            for y in (2, 8, 13):
                px(im, 3, y, (255, 190, 40, 255))
                px(im, 13, y, (255, 190, 40, 255))
        if kind == "ender":
            for y in (4, 9, 14):
                px(im, 3, y, (120, 255, 230, 255))
                px(im, 13, y, (120, 255, 230, 255))
        if kind == "rocket":
            for y in (3, 11):
                px(im, 3, y, (220, 40, 40, 255))
                px(im, 13, y, (220, 40, 40, 255))
    return im


# ---------- иконки вагонеток 16x16 ----------
BODY = {
    "plane": (176, 180, 192), "boat": (58, 150, 98), "slime": (112, 222, 92), "universal": (84, 110, 226),
    "helicopter": (240, 132, 34), "submarine": (34, 160, 176), "ice": (176, 226, 248),
    "lava": (96, 40, 34), "ender": (50, 28, 84), "rocket": (236, 236, 240),
}
OUT = (18, 18, 26, 255)


def cart_icon(kind, classic):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    body = BODY[kind] + (255,)
    rim = sh(body, 1.25) if not classic else body
    d.polygon([(1, 8), (14, 8), (13, 13), (2, 13)], fill=body)
    d.line([(1, 8), (2, 13)], fill=OUT)
    d.line([(14, 8), (13, 13)], fill=OUT)
    d.line([(2, 13), (13, 13)], fill=OUT)
    d.line([(1, 7), (14, 7)], fill=OUT)
    d.line([(2, 8), (13, 8)], fill=rim)
    if not classic:
        d.line([(3, 12), (12, 12)], fill=sh(body, 0.72))
        d.line([(2, 9), (13, 9)], fill=sh(body, 1.12))
    for x in (3, 10):
        d.rectangle([x, 14, x + 2, 15], fill=(58, 58, 66, 255), outline=OUT)
        if not classic:
            px(im, x + 1, 14, (150, 150, 160, 255))

    W = (240, 240, 244, 255)
    R = (206, 46, 46, 255)
    G = (130, 130, 140, 255)
    if kind in ("plane", "universal"):
        d.rectangle([0, 4, 15, 5], fill=W, outline=(120, 120, 130, 255))
        for x in (0, 1, 14, 15):
            px(im, x, 4, R)
            px(im, x, 5, R)
        d.line([(8, 0), (8, 3)], fill=(70, 70, 76, 255))
        d.line([(5, 2), (11, 2)], fill=G)
        d.rectangle([13, 2, 14, 4], fill=R)
        px(im, 6, 9, (140, 210, 250, 255))
        px(im, 7, 9, (140, 210, 250, 255))
    if kind in ("boat", "universal"):
        d.rectangle([0, 13, 15, 13], fill=(122, 86, 50, 255))
        d.line([(1, 15), (14, 15)], fill=(70, 150, 230, 255))
        if kind == "boat":
            d.line([(8, 1), (8, 7)], fill=(110, 80, 48, 255))
            d.polygon([(9, 1), (9, 6), (13, 6)], fill=W, outline=(190, 190, 200, 255))
    if kind in ("slime", "universal"):
        for (x, y) in ((5, 10), (9, 11), (7, 9), (11, 10)):
            px(im, x, y, (196, 255, 176, 255))
        if kind == "slime":
            px(im, 5, 11, (20, 70, 20, 255))
            px(im, 10, 11, (20, 70, 20, 255))
            d.line([(6, 12), (9, 12)], fill=(20, 70, 20, 255))
    if kind == "helicopter":
        d.line([(0, 3), (15, 3)], fill=(60, 60, 66, 255))
        d.line([(0, 2), (3, 2)], fill=G)
        d.line([(12, 2), (15, 2)], fill=G)
        d.line([(7, 4), (7, 6)], fill=(60, 60, 66, 255))
        d.line([(8, 4), (8, 6)], fill=(60, 60, 66, 255))
        d.line([(13, 5), (15, 5)], fill=(60, 60, 66, 255))
        px(im, 4, 10, (150, 220, 250, 255))
        px(im, 5, 10, (150, 220, 250, 255))
    if kind == "submarine":
        d.line([(11, 2), (11, 6)], fill=G)
        d.line([(9, 2), (11, 2)], fill=sh(G, 1.2))
        d.ellipse([6, 9, 9, 12], fill=(250, 214, 80, 255), outline=OUT)
        px(im, 7, 10, (255, 250, 200, 255))
        px(im, 2, 15, (150, 230, 250, 255))
        px(im, 13, 15, (150, 230, 250, 255))
    if kind == "ice":
        for (x, y, h) in ((4, 6, 2), (8, 4, 3), (11, 6, 2)):
            d.line([(x, y), (x, y - h)], fill=(220, 246, 255, 255))
            px(im, x + 1, y, (120, 190, 230, 255))
        d.line([(2, 14), (13, 14)], fill=(120, 190, 230, 255))
        px(im, 6, 10, (255, 255, 255, 255))
        px(im, 10, 11, (255, 255, 255, 255))
    if kind == "lava":
        for (x, y) in ((4, 10), (5, 11), (8, 9), (9, 10), (11, 11)):
            px(im, x, y, (255, 150, 30, 255))
        for (x, y, c) in ((5, 5, (255, 210, 60, 255)), (7, 4, (255, 120, 20, 255)), (9, 5, (255, 210, 60, 255)),
                          (6, 6, (255, 120, 20, 255)), (8, 6, (255, 120, 20, 255)), (7, 5, (255, 240, 120, 255))):
            px(im, x, y, c)
    if kind == "ender":
        d.polygon([(8, 1), (10, 4), (8, 7), (6, 4)], fill=(120, 255, 225, 255), outline=(40, 160, 150, 255))
        px(im, 8, 3, (255, 255, 255, 255))
        for (x, y) in ((4, 10), (11, 9), (6, 12), (9, 11)):
            px(im, x, y, (200, 110, 255, 255))
    if kind == "rocket":
        d.polygon([(11, 8), (11, 12), (15, 10)], fill=R, outline=OUT)
        d.rectangle([4, 4, 6, 7], fill=R)
        d.rectangle([4, 3, 5, 3], fill=R)
        for (x, y) in ((0, 9), (0, 10), (0, 11), (1, 10)):
            px(im, x, y, (255, 170, 40, 255))
        px(im, 0, 10, (255, 240, 120, 255))
        px(im, 7, 10, R)
        px(im, 8, 10, R)
    return im


# ---------- текстуры корпуса 64x32 (раскладка ванильной вагонетки) ----------
HULL = {
    "plane": ((172, 176, 188), (206, 46, 46)),
    "boat": ((54, 132, 96), (140, 98, 56)),
    "slime": ((106, 214, 88), (190, 255, 170)),
    "universal": ((78, 104, 224), (250, 214, 70)),
    "helicopter": ((238, 128, 30), (30, 30, 34)),
    "submarine": ((30, 150, 168), (250, 212, 76)),
    "ice": ((170, 222, 246), (255, 255, 255)),
    "lava": ((70, 38, 36), (255, 140, 30)),
    "ender": ((36, 22, 60), (130, 255, 230)),
    "rocket": ((232, 232, 238), (206, 46, 46)),
}


def body_tex(kind, classic):
    base, acc = HULL[kind]
    base = base + (255,)
    acc = acc + (255,)
    rnd = random.Random(sum(map(ord, kind)))
    im = Image.new("RGBA", (64, 32), base)
    d = ImageDraw.Draw(im)
    if classic:
        d.rectangle([0, 0, 63, 31], outline=sh(base, 0.55))
        d.rectangle([2, 2, 17, 9], outline=sh(base, 0.7))
        d.line([(0, 6), (43, 6)], fill=acc)
        return im
    vgrad(im, (0, 0, 44, 28), sh(base, 1.12), sh(base, 0.86))
    grain(im, (0, 0, 64, 32), 0.06, rnd)
    # стенки: панель с каймой, полоса, заклёпки
    bevel(im, (2, 2, 18, 10), 0.28)
    d.line([(3, 5), (16, 5)], fill=acc)
    d.line([(3, 6), (16, 6)], fill=sh(acc, 0.7))
    rivets(im, (2, 2, 18, 10), 4, sh(base, 1.45))
    # днище: пластины
    bevel(im, (2, 12, 22, 28), 0.22)
    for y in (16, 20, 24):
        d.line([(3, y), (20, y)], fill=sh(base, 0.72))
    rivets(im, (2, 12, 22, 28), 5, sh(base, 1.4))
    # торцы и кромка
    for (x0, y0, x1, y1) in ((0, 2, 2, 10), (18, 2, 20, 10), (2, 0, 18, 2), (18, 0, 34, 2)):
        for x in range(x0, x1):
            for y in range(y0, y1):
                px(im, x, y, sh(base, 0.78 if (x + y) % 2 else 0.9))
    # узоры по типу
    if kind == "plane":
        d.line([(3, 8), (16, 8)], fill=(250, 250, 252, 255))
        for x in range(4, 16, 4):
            px(im, x, 3, sh(acc, 1.1))
    elif kind == "boat":
        for y in (4, 7):
            d.line([(3, y), (16, y)], fill=sh(base, 0.7))
        d.line([(3, 9), (16, 9)], fill=acc)
    elif kind == "slime":
        for _ in range(26):
            x, y = rnd.randrange(0, 44), rnd.randrange(0, 28)
            px(im, x, y, sh(acc, rnd.uniform(0.85, 1.0)))
        for (x, y) in ((6, 4), (12, 4)):
            px(im, x, y, (24, 80, 24, 255))
            px(im, x + 1, y, (24, 80, 24, 255))
    elif kind == "universal":
        d.line([(3, 4), (16, 4)], fill=acc)
        d.line([(3, 8), (16, 8)], fill=(250, 250, 252, 255))
        for x in range(4, 17, 6):
            px(im, x, 6, (200, 255, 180, 255))
    elif kind == "helicopter":
        for x in range(2, 18, 4):
            d.polygon([(x, 8), (x + 2, 8), (x + 3, 9), (x + 1, 9)], fill=acc)
        d.line([(3, 3), (16, 3)], fill=acc)
    elif kind == "submarine":
        d.ellipse([7, 3, 12, 8], fill=(250, 214, 80, 255), outline=sh(base, 0.5))
        px(im, 9, 4, (255, 250, 210, 255))
        d.line([(3, 9), (16, 9)], fill=acc)
    elif kind == "ice":
        for _ in range(34):
            x, y = rnd.randrange(0, 44), rnd.randrange(0, 28)
            px(im, x, y, (255, 255, 255, 255))
        d.line([(3, 3), (8, 8)], fill=(120, 190, 230, 255))
        d.line([(16, 3), (11, 8)], fill=(120, 190, 230, 255))
    elif kind == "lava":
        for (pts) in (((3, 3), (6, 5), (8, 4), (12, 7)), ((10, 3), (12, 5), (15, 4))):
            d.line(pts, fill=acc)
        for _ in range(16):
            px(im, rnd.randrange(0, 44), rnd.randrange(0, 28), (255, 210, 70, 255))
        for y in (18, 23):
            d.line([(4, y), (9, y + 1), (14, y)], fill=acc)
    elif kind == "ender":
        for _ in range(34):
            x, y = rnd.randrange(0, 44), rnd.randrange(0, 28)
            px(im, x, y, rnd.choice(((150, 70, 230, 255), (130, 255, 230, 255), (230, 120, 255, 255))))
        d.line([(3, 6), (16, 6)], fill=acc)
    elif kind == "rocket":
        for x in range(2, 18, 4):
            d.rectangle([x, 2, x + 1, 9], fill=acc)
        d.line([(3, 6), (16, 6)], fill=(40, 40, 46, 255))
    return im


# ---------- детали 256x256 (крылья, винты, поплавки...) ----------
def extras_tex(classic):
    rnd = random.Random(99)
    im = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    W = (238, 238, 244, 255)
    RED = (204, 46, 46, 255)
    METAL = (150, 152, 162, 255)
    DARK = (58, 58, 66, 255)
    WOOD = (124, 88, 52, 255)

    def fill(x, y, w, h, c, bev=True, grn=0.05, vert=None):
        d.rectangle([x, y, x + w - 1, y + h - 1], fill=c)
        if classic:
            return
        if vert:
            vgrad(im, (x, y, x + w, y + h), vert[0], vert[1])
        grain(im, (x, y, x + w, y + h), grn, rnd)
        if bev:
            bevel(im, (x, y, x + w, y + h), 0.2)

    # крылья: верх и низ (12 x 56) с законцовками, полосой и кругом
    fill(0, 0, 136, 57, W, vert=((250, 250, 255), (210, 212, 222)))
    for ox in (56, 68):
        for y in list(range(0, 6)) + list(range(50, 56)):
            d.line([(ox, y), (ox + 11, y)], fill=RED)
        d.line([(ox + 5, 6), (ox + 5, 49)], fill=(70, 100, 200, 255) if not classic else (120, 120, 200, 255))
        for y in (14, 28, 42):
            d.line([(ox, y), (ox + 11, y)], fill=(176, 178, 190, 255))
        d.ellipse([ox + 2, 20, ox + 9, 27], fill=(204, 46, 46, 255), outline=(250, 250, 252, 255))
    # хвост
    fill(0, 60, 10, 10, RED)
    fill(0, 72, 36, 15, W, vert=((250, 250, 255), (205, 208, 218)))
    d.rectangle([14, 72, 17, 85], fill=RED)
    # лопасти и втулка
    fill(140, 0, 6, 16, (70, 72, 80, 255))
    fill(150, 0, 30, 16, (70, 72, 80, 255))
    fill(190, 0, 12, 9, (170, 172, 182, 255))
    d.rectangle([140, 0, 145, 2], fill=(230, 190, 60, 255))
    d.rectangle([150, 0, 156, 1], fill=(230, 190, 60, 255))
    # поплавки лодки: дерево с зелёной кромкой
    for y in (90, 100):
        fill(0, y, 70, 7, WOOD, vert=((150, 108, 64, 255), (100, 70, 40, 255)))
        d.line([(0, y), (69, y)], fill=(40, 176, 76, 255))
        for x in range(8, 70, 10):
            px(im, x, y + 3, (70, 50, 30, 255))
    # поплавки лавовой: чёрный камень со свечением
    for y in (110, 120):
        fill(0, y, 70, 7, (54, 40, 40, 255), vert=((78, 56, 52, 255), (38, 28, 28, 255)))
        for x in range(4, 70, 9):
            px(im, x, y + 3, (255, 140, 30, 255))
            px(im, x + 1, y + 3, (255, 200, 60, 255))
        d.line([(0, y + 6), (69, y + 6)], fill=(214, 70, 24, 255))
    # ротор вертолёта
    fill(0, 130, 94, 4, (60, 62, 70, 255))
    fill(100, 130, 94, 45, (60, 62, 70, 255))
    d.rectangle([0, 130, 6, 133], fill=(240, 150, 40, 255))
    d.rectangle([100, 130, 112, 135], fill=(240, 150, 40, 255))
    # мачта вертолёта, перископ, рубка подлодки
    fill(140, 30, 8, 7, (80, 82, 90, 255))
    fill(200, 30, 8, 15, (150, 152, 162, 255))
    d.rectangle([200, 30, 207, 33], fill=(60, 62, 70, 255))
    fill(0, 180, 32, 11, (34, 150, 168, 255), vert=((60, 190, 206, 255), (24, 112, 128, 255)))
    d.ellipse([10, 183, 14, 187], fill=(250, 214, 80, 255), outline=DARK)
    # лыжи
    for y in (200, 210):
        fill(0, y, 78, 5, (160, 220, 250, 255), vert=((220, 246, 255, 255), (110, 180, 224, 255)))
        for x in range(6, 78, 12):
            px(im, x, y + 2, (255, 255, 255, 255))
    # нос и сопло ракеты
    fill(100, 180, 24, 14, RED, vert=((240, 90, 80, 255), (168, 30, 34, 255)))
    d.line([(100, 186), (123, 186)], fill=(250, 250, 252, 255))
    fill(140, 180, 18, 11, (70, 72, 80, 255), vert=((100, 102, 112, 255), (40, 42, 48, 255)))
    d.rectangle([141, 181, 144, 184], fill=(255, 170, 40, 255))
    # кристалл эндера
    fill(200, 180, 24, 12, (120, 255, 226, 255), vert=((200, 255, 245, 255), (60, 180, 200, 255)))
    for x in range(204, 224, 4):
        px(im, x, 184, (255, 255, 255, 255))
        px(im, x + 1, 186, (200, 110, 255, 255))
    return im


def save(im, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)


for classic, base in ((False, A), (True, CL)):
    for k in RAIL_KINDS:
        save(rail_tex(k, False, classic), f"{base}/textures/block/{k}_rail.png")
        save(rail_tex(k, True, classic), f"{base}/textures/block/{k}_rail_on.png")
    for k in KINDS:
        save(cart_icon(k, classic), f"{base}/textures/item/{k}_cart.png")
        save(body_tex(k, classic), f"{base}/textures/entity/{k}_cart.png")
    save(extras_tex(classic), f"{base}/textures/entity/cart_extras.png")
print("ok")
