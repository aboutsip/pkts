stream = '5700090086076939d8d4176b8a'

variable_name = 'fteid'

declaration = '    public static final Buffer ' + variable_name + ' = Buffer.of('
print(declaration)
for index in range(0, len(stream), 2):
    end = ','
    if index % 16 == 14:
        end = ',\n'
    if index % 16 == 0:
        print('        ', end = '')
    if index + 1 == len(stream) - 1:
        end = ');\n'
    print("(byte) 0x" + stream[index] + stream[index + 1], end = end)
