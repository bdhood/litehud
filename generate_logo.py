from PIL import Image, ImageDraw, ImageFilter

size = 128
img = Image.new('RGBA', (size, size), (0,0,0,0))
draw = ImageDraw.Draw(img)

r = 18
draw.rounded_rectangle([0, 0, size-1, size-1], radius=r, fill=(48, 56, 76, 255))
draw.rounded_rectangle([2, 2, size-3, size-3], radius=r-2, fill=(16, 18, 24, 255))

bar = 9
lw = 32
hw = 32
gap = 11
total_w = lw + gap + hw
lh = 54
lx = (size - total_w) // 2
ty = (size - lh) // 2 - 2
hx = lx + lw + gap

# Glow
glow = Image.new('RGBA', (size, size), (0,0,0,0))
gd = ImageDraw.Draw(glow)
gc = (90, 230, 160, 100)
gd.rectangle([lx-2, ty-2, lx+bar+1, ty+lh+1], fill=gc)
gd.rectangle([lx-2, ty+lh-bar-2, lx+lw+1, ty+lh+1], fill=gc)
gd.rectangle([hx-2, ty-2, hx+bar+1, ty+lh+1], fill=gc)
gd.rectangle([hx+hw-bar-2, ty-2, hx+hw+1, ty+lh+1], fill=gc)
mid = ty + lh//2 - bar//2
gd.rectangle([hx-2, mid-2, hx+hw+1, mid+bar+1], fill=gc)
glow = glow.filter(ImageFilter.GaussianBlur(radius=4))
img = Image.alpha_composite(img, glow)

# Gradient letters
sharp = Image.new('RGBA', (size, size), (0,0,0,0))
sd = ImageDraw.Draw(sharp)
for y in range(lh):
    t = y / max(lh-1, 1)
    col = (int(175 - t*35), int(255 - t*45), int(210 - t*35), 255)
    py = ty + y
    sd.line([(lx, py), (lx+bar-1, py)], fill=col)
    if py >= ty + lh - bar:
        sd.line([(lx, py), (lx+lw-1, py)], fill=col)
    sd.line([(hx, py), (hx+bar-1, py)], fill=col)
    sd.line([(hx+hw-bar, py), (hx+hw-1, py)], fill=col)
    if ty + lh//2 - bar//2 <= py < ty + lh//2 - bar//2 + bar:
        sd.line([(hx, py), (hx+hw-1, py)], fill=col)
img = Image.alpha_composite(img, sharp)

# Corner brackets
al = Image.new('RGBA', (size, size), (0,0,0,0))
ad = ImageDraw.Draw(al)
ac = (90, 190, 140, 180)
bl, bt, mg = 7, 2, 9
corners = [
    (mg, mg, mg+bl, mg+bt, mg, mg, mg+bt, mg+bl),
    (size-1-mg-bl, mg, size-1-mg, mg+bt, size-1-mg-bt, mg, size-1-mg, mg+bl),
    (mg, size-1-mg-bt, mg+bl, size-1-mg, mg, size-1-mg-bl, mg+bt, size-1-mg),
    (size-1-mg-bl, size-1-mg-bt, size-1-mg, size-1-mg, size-1-mg-bt, size-1-mg-bl, size-1-mg, size-1-mg),
]
for c in corners:
    ad.rectangle([c[0], c[1], c[2], c[3]], fill=ac)
    ad.rectangle([c[4], c[5], c[6], c[7]], fill=ac)
img = Image.alpha_composite(img, al)

out = 'src/main/resources/logo.png'
img.save(out)
print(f'Saved to {out}')