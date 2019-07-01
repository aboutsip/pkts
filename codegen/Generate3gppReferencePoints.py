import csv
import json
from liquid import Liquid

def format(s):
    return '"{}"'.format(s)

def loadCsv(file_name = '3gpp_itu_interfaces.tsv'):
    interfaces = []
    with open(file_name) as file:
        reader = csv.reader(file, delimiter = '\t')
        next(reader)
        for row in reader:
            name = row[0]
            if name.endswith('’') or name.endswith("′"):
                name = name[:len(name) - 1 ] + 'Prime'
            name = name.replace('-', '_')

            protocol = [format(p) for p in row[1].split(',')]
            elements = [format(e) for e in row[2].split(',')]
            description = format(row[3])
            references = [format(r.replace('TS','')) for r in row[4].split(' ') if r != 'TS']

            interfaces.append({
                'name' : name,
                'protocol' : 'new String[]{' + ','.join(protocol) + '}',
                'elements' : 'new String[]{' + ','.join(elements) + '}',
                'description' : description,
                'references' : 'new String[]{' + ','.join(references) + '}',
            })
    return interfaces;


def render(interfaces, template):
    with open(template) as f:
        content = f.read()
        liq = Liquid(content)
        res = liq.render(interfaces = interfaces)

    return res

if __name__ == '__main__':
    interfaces = loadCsv()
    # print(json.dumps(interfaces, indent = 3) )
    res = render(interfaces, 'reference_points.liquid')
    print(res)

    java_file = '../pkts-3gppitu/src/main/java/io/pkts/tgpp/ReferencePoint.java'
    with open(java_file, 'w') as java:
        java.write(res)
