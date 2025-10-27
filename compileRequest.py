# [+] Compile Request
import threading
import os

def _wanted_(character):
    return character in "0123456789ABCDEF"

allowedCharacters = [chr(ordinal) for ordinal in range(128)]
characterCodePointFilter = [c if _wanted_(c) else None for c in allowedCharacters]

def _fastReplace_(string):
    # Remove all non-ASCII characters. Heavily optimised.
    string = string.encode('ascii', errors='ignore').decode('ascii')

    # Remove unwanted ASCII characters
    return string.translate(characterCodePointFilter)

def image():
    print("[+] Attempting to compile image!")

    with open('output/img.png', 'w') as clearImage:
        clearImage.truncate(0)
    
    with open('output/output.txt', "r") as inputData:
        data = inputData.read()
    
    print(data.replace(" ", "").replace("\n", "")[0:250])

    try:
        bytes.fromhex(_fastReplace_(data))
    except ValueError:
        data += "0"
    finally:
        # data = bytes.fromhex(data.replace(" ", "").replace("\n", ""))
        data = bytes.fromhex(_fastReplace_(data))


    with open('output/img.png', 'wb') as compileImage:
        compileImage.write(data)

    print("[+] Compiled image!")
    
    return "image"

