import csv
import json
from liquid import Liquid

def format(s):
    return '"{}"'.format(s)

def is_selected(value):
    return value == 'x' or value == 'X'

def check_empty(value):
    if not value or value == '':
        return '""'

def append_interface(value, name, interfaces):
    if is_selected(value):
        interfaces.append(name)

def loadCsv(file_name = 'GTPv2_Information_Elements.tsv'):
    elements = []
    with open(file_name) as file:
        reader = csv.reader(file, delimiter = '\t')
        next(reader)
        for row in reader:
            if row[0] == 'x':
                continue

            type = int(row[1])
            message = row[2].replace(' ', '_')
            specification = format(row[3])

            section = check_empty(row[4])
            initial = 'true' if is_selected(row[5]) else 'false'
            triggered = 'true' if is_selected(row[6]) else 'false'

            list = []
            append_interface(row[7], 'S3', list)
            append_interface(row[8], 'S10', list)
            append_interface(row[9], 'S16', list)
            append_interface(row[10], 'N26', list)
            append_interface(row[11], 'S11', list)
            append_interface(row[12], 'S4', list)
            append_interface(row[13], 'S5', list)
            append_interface(row[14], 'S8', list)
            append_interface(row[15], 'S2a', list)
            append_interface(row[16], 'S2b', list)
            interfaces = ','.join(list)

            elements.append({
                'type' : type,
                'message' : message,
                'specification' : specification,
                'section' : section,
                'initial' : initial,
                'triggered' : triggered,
                'interfaces' : interfaces,
            })
    return elements;


def render(elements, template):
    with open(template) as f:
        content = f.read()
        liq = Liquid(content)
        res = liq.render(elements = elements)

    return res

if __name__ == '__main__':
    elements = loadCsv()
    print(json.dumps(elements, indent = 3) )
    res = render(elements, 'gtpv2_information_elements.liquid')
    print(res)

    java_file = '../pkts-3gppitu/src/main/java/io/pkts/tgpp/Gtp2InformationElements.java'
    with open(java_file, 'w') as java:
        java.write(res)
