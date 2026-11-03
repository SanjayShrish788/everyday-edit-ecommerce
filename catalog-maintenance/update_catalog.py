"""Apply the Everyday Edit catalog through the existing API. Re-running updates by name."""
import json, pathlib, urllib.request, uuid, mimetypes, os
ROOT = pathlib.Path(__file__).resolve().parent
# Override when seeding a different local API instance.
API = os.environ.get('CATALOG_API_URL', 'http://localhost:8081/api').rstrip('/')
def get(url):
    """Fetch bytes and their MIME type; image hosts may require a browser user agent."""
    with urllib.request.urlopen(urllib.request.Request(url,headers={"User-Agent":"Mozilla/5.0"}), timeout=45) as r: return r.read(), r.headers.get_content_type()
def products():
    """Read committed product records through the existing catalog endpoint."""
    return json.loads(get(API+'/products')[0])
existing = products()
backup = ROOT/'catalog-before.json'
if not backup.exists(): backup.write_text(json.dumps(existing,indent=2),encoding='utf-8')
source = json.loads(get('https://dummyjson.com/products?limit=0')[0])['products']
by_title={p['title']:p for p in source}
entries=[
 dict(id=4,name='Dell XPS 13',brand='Dell',price=94990,category='Laptop',description='Compact Dell XPS 13 laptop for work, study, and everyday creativity.',source='New DELL XPS 13 9300 Laptop'),
 dict(id=1,asset='iphone-14-pro.jpg',name='iPhone 14 Pro',brand='Apple',price=79990,category='Mobile',description='Apple iPhone 14 Pro in Deep Purple, with Dynamic Island and a triple-camera system.'),
 dict(id=5,asset='straight-fit-jeans.jpg',name="Levi’s Straight Fit Jeans",brand="Levi’s",price=2999,category='Fashion',description='Classic blue denim jeans with a straight fit and versatile everyday styling.'),
 dict(id=6,name='LEGO Disney Pixar Up House',brand='LEGO',price=5999,category='Toys',description='Disney and Pixar Up House building set 43217. A colorful display and imaginative build for ages 9+.',image='https://cdn.shopify.com/s/files/1/0412/6792/1045/products/43217product.png?v=1679285570&width=700'),
 dict(id=7,name='Colgate Triple Action Twin Pack',brand='Colgate',price=249,category='Personal Care',description='Colgate Triple Action original mint toothpaste, twin pack of 175 g tubes.',image='https://cdn.shopify.com/s/files/1/0257/1884/9583/files/58569_new.jpg?v=1765246520&width=700'),
 dict(name='MacBook Pro 14-inch',brand='Apple',price=159900,category='Laptop',description='14-inch MacBook Pro in Space Grey for creative work and everyday productivity.',source='Apple MacBook Pro 14 Inch Space Grey'),
 dict(name='Lenovo Yoga 920',brand='Lenovo',price=79990,category='Laptop',description='Versatile Yoga convertible laptop for flexible work and entertainment.',source='Lenovo Yoga 920'),
 dict(name='Apple AirPods',brand='Apple',price=12900,category='Headphone',description='Wireless earbuds with a charging case for everyday music and calls.',source='Apple Airpods'),
 dict(name='AirPods Max — Silver',brand='Apple',price=59900,category='Headphone',description='Over-ear wireless headphones in Silver with a comfortable cushioned design.',source='Apple AirPods Max Silver'),
 dict(name='Amazon Echo Plus',brand='Amazon',price=10999,category='Electronics',description='Smart speaker for music, voice assistance, and compatible smart-home devices.',source='Amazon Echo Plus'),
 dict(name='Blue & Black Check Shirt',brand='Everyday Edit',price=1499,category='Fashion',description='A relaxed everyday shirt in a blue and black check pattern.',source='Blue & Black Check Shirt'),
 dict(name='Puma Future Rider Trainers',brand='Puma',price=6499,category='Fashion',description='Sport-inspired casual trainers with a comfortable everyday silhouette.',source='Puma Future Rider Trainers'),
 dict(name='OPPO A57',brand='OPPO',price=13999,category='Mobile',description='An everyday smartphone for browsing, photos, calls, and entertainment.',source='Oppo A57'),
]
# Download every replacement before changing records. Images are uploaded as bytes, not remote links.
assets=ROOT/'images'; assets.mkdir(exist_ok=True)
for entry in entries:
    url=entry.get('image') or (by_title[entry['source']]['images'][0] if entry.get('source') else None)
    if entry.get('asset'):
        entry['_asset']=((ROOT/'assets'/entry['asset']).read_bytes(), 'image/jpeg')
    if url:
        blob,mime=get(url)
        if not mime.startswith('image/'): raise ValueError('Not an image: '+url)
        entry['_asset']=(blob,mime)
        (assets/(str(entry.get('id',entry['name'].replace(' ','-')))+(mimetypes.guess_extension(mime) or '.img'))).write_bytes(blob)
for entry in entries:
    old=next((p for p in existing if p['id']==entry.get('id') or p['name']==entry['name']),None)
    product={k:v for k,v in (old or {}).items() if k not in ('imageData','imageName','imageType')}
    product.update({k:v for k,v in entry.items() if k not in ('source','image','asset','_asset')})
    product.setdefault('stockQuantity',30); product.setdefault('releaseDate','2026-01-01T00:00:00.000+00:00'); product['productAvailable']=product['stockQuantity']>0
    if old:
        product['id']=old['id']
    else:
        # Let PostgreSQL generate IDs when importing into an empty database.
        product.pop('id', None)
    boundary='Catalog'+uuid.uuid4().hex
    body=bytearray()
    def part(name,content,mime,filename=None):
        body.extend(('--'+boundary+'\r\nContent-Disposition: form-data; name="'+name+'"'+(' ; filename="'+filename+'"' if filename else '')+'\r\nContent-Type: '+mime+'\r\n\r\n').encode())
        body.extend(content);body.extend(b'\r\n')
    part('product',json.dumps(product).encode(),'application/json')
    if '_asset' in entry:
        blob,mime=entry['_asset'];part('imageFile',blob,mime,'product'+(mimetypes.guess_extension(mime) or '.img'))
    body.extend(('--'+boundary+'--\r\n').encode())
    url=API+'/product'+('/'+str(old['id']) if old else '')
    req=urllib.request.Request(url,data=bytes(body),method='PUT' if old else 'POST',headers={'Content-Type':'multipart/form-data; boundary='+boundary})
    with urllib.request.urlopen(req,timeout=45) as response: saved=json.load(response)
    print(str(saved['id'])+' | '+saved['name']+' | INR '+str(saved['price']))
updated=products()
(ROOT/'catalog-after.json').write_text(json.dumps([{k:v for k,v in p.items() if k!='imageData'} for p in updated],indent=2),encoding='utf-8')
for p in updated:
    blob,mime=get(API+'/product/'+str(p['id'])+'/image')
    assert len(blob)>500 and mime.startswith('image/'),p['name']
print('Verified',len(updated),'products with persisted image responses.')


