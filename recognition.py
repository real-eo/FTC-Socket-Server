from keras.models import load_model
from PIL import Image, ImageOps #Install pillow instead of PIL
import numpy as np

def getRecognition():
    np.set_printoptions(suppress=True)

    # Load the model
    model = load_model('data/keras_model.h5', compile=False)

    # Load the labels
    classNames = open('data/labels.txt', 'r').readlines()

    # Create the array of the right shape to feed into the keras model
    # The 'length' or number of images you can put into the array is determined by the first position in the shape tuple, in this case 1.
    data = np.ndarray(shape=(1, 224, 224, 3), dtype=np.float32)

    image = Image.open('output/img.png').convert('RGB')

    # Resize the image to a 224x224 with the same strategy as in TM2:
    # Resizing the image to be at least 224x224 and then cropping from the center
    size = (224, 224)
    image = ImageOps.fit(image, size, Image.Resampling.LANCZOS)

    imageArray = np.asarray(image)  # Turn the image into a numpy array
    normalizedImageArray = (imageArray.astype(np.float32) / 127.0) - 1  # Normalize the image

    # Load the image into the array
    data[0] = normalizedImageArray

    # run the inference
    prediction = model.predict(data)
    index = np.argmax(prediction)
    className = classNames[index]
    confidenceScore = prediction[0][index]

    print('Class:', className, end='')
    print('Confidence score:', confidenceScore)

    return className