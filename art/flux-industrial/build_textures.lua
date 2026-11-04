-- Run with Aseprite MCP. Export under its supplied workspace, then stage into the pack.
-- AI material tiles are remapped to the original mod's exact texture/UV dimensions.
local root=app.params.aseprite_mcp_workspace..'/revo-flux-tech'
local out=root..'/exports'
app.fs.makeAllDirectories(out..'/block');app.fs.makeAllDirectories(out..'/item');app.fs.makeAllDirectories(out..'/gui');app.fs.makeAllDirectories(root..'/sources')
local atlas=Image{fromFile=root..'/materials-generated.png'}
local pc=app.pixelColor
local function c(r,g,b,a)return pc.rgba(r,g,b,a or 255)end
local ink=c(23,26,28);local steel=c(92,101,105);local light=c(158,165,160);local copper=c(162,100,53);local yellow=c(91,149,151);local rust=c(105,64,39);local white=c(235,235,235)
local function put(im,x,y,color)if x>=0 and y>=0 and x<im.width and y<im.height then im:drawPixel(x,y,color)end end
local function rect(im,x,y,w,h,color)for Y=y,y+h-1 do for X=x,x+w-1 do put(im,X,Y,color)end end end
local function outline(im,x,y,w,h,color)rect(im,x,y,w,1,color);rect(im,x,y+h-1,w,1,color);rect(im,x,y,1,h,color);rect(im,x+w-1,y,1,h,color)end
local function tile(n,w,h)
 local im=Image(w,h,ColorMode.RGB);local tx=n%4;local ty=math.floor(n/4)
 for y=0,h-1 do for x=0,w-1 do
  local X=math.min(atlas.width-1,math.floor((tx+(x+.5)/w)*atlas.width/4));local Y=math.min(atlas.height-1,math.floor((ty+(y+.5)/h)*atlas.height/4))
  local v=atlas:getPixel(X,Y);im:drawPixel(x,y,c(pc.rgbaR(v),pc.rgbaG(v),pc.rgbaB(v)))
 end end;return im
end
local function patch(im,n,x,y,w,h)local src=tile(n,w,h);im:drawImage(src,Point(x,y))end
local function grime(im,amount)
 -- Quiet edge wear, not scattered grime. Keep a coherent low-contrast palette.
 for y=1,im.height-2 do for x=1,im.width-2 do
  if (x*47+y*67)%139==0 then local v=im:getPixel(x,y);if pc.rgbaA(v)>0 then put(im,x,y,c(math.min(255,pc.rgbaR(v)+5),math.min(255,pc.rgbaG(v)+5),math.min(255,pc.rgbaB(v)+5),pc.rgbaA(v)))end end
 end end
end
local function hazard(im,x,y,w,h)
 -- Replaces the former hazard bands with continuous steel trim.
 rect(im,x,y,w,h,steel);rect(im,x,y,w,1,light);if h>1 then rect(im,x,y+h-1,w,1,ink)end
end
local function bolt(im,x,y)put(im,x,y,ink);put(im,x+1,y,light);put(im,x+1,y+1,steel)end
local function save(name,im,detail)
 local spr=Sprite(im.width,im.height,ColorMode.RGB);spr.layers[1].name='Material / UV base';spr.cels[1].image=im
 if detail then local l=spr:newLayer();l.name='Hardware, steel trim and subtle wear';spr:newCel(l,1,detail,Point(0,0))end
 spr:saveAs(root..'/sources/'..name:gsub('/','_')..'.aseprite');spr:saveCopyAs(out..'/'..name..'.png');spr:close();print(name..' '..im.width..'x'..im.height)
end
local function fresh(w,h)return Image(w,h,ColorMode.RGB)end

-- Solid housing and UV swatches.
local im=tile(0,16,16);local d=fresh(16,16);hazard(d,2,12,12,3);grime(im,8);save('block/flux_block',im,d)
for _,v in ipairs{{'gray',1},{'darkblue',14},{'midblue',1},{'lightblue',3}}do local t=tile(v[2],16,16);grime(t,10);save('block/'..v[1],t)end
local t=tile(4,16,16);hazard(t,1,12,14,3);save('block/fluxcontroller_bottom',t)
t=tile(2,16,16);rect(t,8,0,8,16,steel);hazard(t,8,3,8,3);hazard(t,8,10,8,3);grime(t,7);save('block/flux_controller',t)
for _,active in ipairs{false,true}do
 local top=tile(5,16,16);local de=fresh(16,16);hazard(de,1,12,14,3);rect(de,11,8,3,2,active and yellow or c(81,76,60));grime(top,7);save('block/fluxcontroller_'..(active and 'on' or 'off'),top,de)
end
local mask=fresh(16,16);rect(mask,5,2,6,1,white);rect(mask,13,5,1,5,white);save('block/fluxcontroller_colour',mask)

-- 32px connector atlas: the device cube uses UV [8,2,16,10] in 16-unit UVs.
-- Preserve these islands, including the separate cable/end-cap samples.
for _,kind in ipairs{'plug','point'}do
 for _,active in ipairs{false,true}do
  local base=tile(1,32,32);patch(base,6,16,4,16,16);local det=fresh(32,32)
  rect(det,0,0,4,12,ink);rect(det,1,0,2,12,copper);hazard(det,0,5,4,3)
  rect(det,19,7,10,8,ink);outline(det,18,6,12,10,steel)
  -- Chunky physical direction arrow and a small status LED.
  local col=active and c(172,198,194) or c(88,103,109);rect(det,23,9,2,5,col)
  if kind=='plug' then rect(det,21,10,6,1,col);rect(det,22,9,4,1,col);rect(det,23,8,2,1,col)
  else rect(det,21,12,6,1,col);rect(det,22,13,4,1,col);rect(det,23,14,2,1,col)end
  hazard(det,17,17,14,3);rect(det,27,7,2,2,active and c(103,172,168) or c(62,81,83))
  bolt(det,16,4);bolt(det,29,4);bolt(det,16,14);bolt(det,29,14);grime(base,12)
  save('block/flux_'..kind..'_'..(active and 'on' or 'off'),base,det)
 end
 local m=fresh(32,32);rect(m,19,6,7,1,white);rect(m,29,10,1,5,white);rect(m,1,0,2,3,white);save('block/flux_'..kind..'_colour',m)
end
for _,v in ipairs{{'flux_cable',11},{'flux_cable_basic',11},{'flux_cable_blue',2}}do local b=tile(v[2],32,32);hazard(b,0,12,8,3);grime(b,10);save('block/'..v[1],b)end

-- Capacitor-bank frames: grey bars, stepped tier markings and network-coloured LEDs.
for tier=1,3 do
 local b=tile(7+tier,16,16);local de=fresh(16,16)
 outline(de,0,0,16,16,ink);rect(de,1,1,14,1,light);hazard(de,2,12,12,3)
 for i=1,tier do rect(de,2+(i-1)*4,3,2,2,copper)end
 bolt(de,1,1);bolt(de,13,1);bolt(de,1,13);bolt(de,13,13);grime(b,8);save('block/tier'..tier,b,de)
 local off=tile(14,16,16);outline(off,0,0,16,16,ink);hazard(off,1,12,14,3);grime(off,8);save('block/tier'..tier..'_off',off)
 local m=fresh(16,16);for i=1,tier do rect(m,3+(i-1)*4,2,2,1,white)end;rect(m,1,6,1,4,white);save('block/tier'..tier..'_colour',m)
end
t=tile(8,32,32);hazard(t,1,27,30,4);grime(t,10);save('block/flux_storage',t)
-- The mod animates charge height; this texture makes it read as stacked battery bars.
mask=fresh(32,32);for y=0,31 do for x=0,31 do if y%6<4 and x%8<6 then put(mask,x,y,((x+y)%5==0) and c(158,158,158) or white)end end end;save('block/flux_storage_energy',mask)

-- Handheld industrial multimeter, with a separate network-colour display layer.
local function meter(admin)
 local b=fresh(32,32);local de=fresh(32,32)
 rect(b,7,3,18,24,ink);rect(b,8,4,16,22,steel);rect(b,7,8,2,16,ink);rect(b,23,8,2,16,ink)
 rect(b,10,6,12,8,steel);rect(b,11,7,10,6,ink);rect(b,12,8,8,4,admin and c(137,57,41) or c(108,143,121))
 rect(b,12,9,5,1,ink);rect(b,17,10,2,1,ink);rect(b,14,11,5,1,ink)
 rect(b,13,17,7,6,ink);rect(b,15,16,3,8,steel);rect(b,14,18,5,2,light)
 rect(b,11,26,2,4,ink);rect(b,20,26,2,4,copper);rect(b,12,29,10,2,ink)
 hazard(b,9,24,14,2);bolt(b,8,4);bolt(b,22,4);grime(b,10);rect(de,19,14,3,2,white)
 return b,de
end
local b,de=meter(false);save('item/flux_configurator_layer0',b);save('item/flux_configurator_layer1',de);local merged=Image(b);merged:drawImage(de,Point(0,0));save('item/flux_configurator_flat',merged)
b,de=meter(true);b:drawImage(de,Point(0,0));save('item/admin_configurator',b)
b=fresh(32,32);patch(b,15,5,6,22,20);outline(b,4,5,24,22,ink);rect(b,8,11,16,11,copper);patch(b,2,10,12,12,9);rect(b,8,10,16,2,steel);rect(b,8,21,16,2,steel);for _,x in ipairs{5,25}do for _,y in ipairs{6,24}do bolt(b,x,y)end end;hazard(b,6,25,20,2);grime(b,8);save('item/flux_core',b)
b=fresh(16,16);for _,v in ipairs{{3,10,5,3},{8,8,4,3},{5,5,3,3},{11,11,3,2},{1,12,3,2},{9,3,2,3}}do rect(b,v[1],v[2],v[3],v[4],ink);rect(b,v[1],v[2],v[3]-1,v[4]-1,copper)end;grime(b,8);save('item/flux_dust',b)

-- Keep GUI dimensions, alpha outlines and icon hit areas exactly as supplied by the mod.
-- Only the background/frame and recipe plate are repainted; functional icon glyphs stay legible.
for _,name in ipairs{'gui_background','gui_frame','gui_icon','inventory_configuration','gui_creating_flux_recipe'}do
 local src=Image{fromFile=root..'/original/gui/'..name..'.png'};local im=Image(src.width,src.height,ColorMode.RGB)
 for y=0,src.height-1 do for x=0,src.width-1 do
  local old=src:getPixel(x,y);local a=pc.rgbaA(old)
  if a>0 then
   local r,g,b=pc.rgbaR(old),pc.rgbaG(old),pc.rgbaB(old)
   if name=='gui_background' then
    local h=(x*31+y*17+x*y)%71;local tone=h<5 and -4 or h>65 and 3 or 0;r=30+tone;g=34+tone;b=36+tone
    if y>src.height-23 and y<src.height-17 and x>20 and x<src.width-20 then r=84;g=100;b=105 end
    a=math.max(a,235)
   elseif name=='gui_frame' then r=138;g=144;b=137
   elseif name=='gui_creating_flux_recipe' and (r+g+b)/3>80 then r=115;g=121;b=119
   elseif name=='gui_icon' then local br=math.max(r,g,b);r=math.floor(br*.94);g=math.floor(br*.94);b=math.floor(br*.87)
   end
   put(im,x,y,c(r,g,b,a))
  end
 end end
 save('gui/'..name,im)
end
print('Industrial grid texture export complete')

app.fs.makeAllDirectories(out..'/converter')
for _,v in ipairs{{'side',4},{'top',7},{'front',13},{'back',6}}do
 local b=tile(v[2],16,16);local de=fresh(16,16)
 if v[1]=='front' or v[1]=='back' then rect(de,3,3,10,10,ink);outline(de,2,2,12,12,steel);rect(de,5,5,6,6,v[1]=='front' and c(83,144,150) or copper);rect(de,6,6,4,4,ink);bolt(de,2,2);bolt(de,12,12)end
 save('converter/'..v[1],b,de)
end
