local root=app.params.aseprite_mcp_workspace..'/revo-item-refresh';app.fs.makeAllDirectories(root..'/exports');app.fs.makeAllDirectories(root..'/sources')
local src=Image{fromFile=root..'/generated-sheet.png'};local pc=app.pixelColor
local names={'unfired_clay_bucket','clay_bucket','clay_water_bucket','clay_lava_bucket','crook'}
for i,name in ipairs(names)do
 local im=Image(16,16,ColorMode.RGB);local col=(i-1)%3;local row=(i-1)//3
 for y=0,15 do for x=0,15 do local X=math.floor(col*src.width/3+32+(x+.5)*448/16);local Y=math.floor(row*src.height/2+32+(y+.5)*448/16);local p=src:getPixel(X,Y)
  if pc.rgbaA(p)>200 then im:drawPixel(x,y,pc.rgba(pc.rgbaR(p),pc.rgbaG(p),pc.rgbaB(p),255))end
 end end
 local spr=Sprite(16,16,ColorMode.RGB);spr.layers[1].name='Clay or wood pixel sprite';spr.cels[1].image=im;spr:saveAs(root..'/sources/'..name..'.aseprite');spr:saveCopyAs(root..'/exports/'..name..'.png');spr:close();print(name)
end
local original=app.params.aseprite_mcp_workspace..'/revo-flux-tech/exports/converter'
for tier=0,2 do
 local dest=root..'/tiers/'..tier;app.fs.makeAllDirectories(dest)
 for _,name in ipairs{'front','back','top','side'}do
  local im=Image{fromFile=original..'/'..name..'.png'}
  if name=='front' or name=='top' then for n=0,tier do for x=0,1 do im:drawPixel(5+n*3+x,3,pc.rgba(168,202,202,255))end end end
  local spr=Sprite(16,16,ColorMode.RGB);spr.cels[1].image=im;spr:saveAs(root..'/sources/converter_'..tier..'_'..name..'.aseprite');spr:saveCopyAs(dest..'/'..name..'.png');spr:close()
 end
end
