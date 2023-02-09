# TextureGenerator

This project is an attempt to replicate certain tools from common photo editing and shading applications like Photoshop and GIMP. It works by representing images as 2D arrays
of floats from 0.0 to 1.0, the former being black and the latter being white. All methods are just different ways of manipulating those values. In order to create and
manipulate colored images, they can be represented as 3 separate black and white images (one for r channel, one for g channel, one for b channel), and then combined using the
TextureColorer class.

Guide:

Currently, there are no console based interactions with the code in TextureGenerator, so editing images is done by directly editing the main method to call the different
editing methods. To start, set the resolution class constant to the size of the image you want to create. Images created using the constant will always be 1:1 aspect ratio, 
but other images with different resolutions can be edited by some methods. Make sure not to remove the declarations of the DrawingPanel, Graphics, and Random objects in the 
main method. Everything else can be changed.

Start with a 2D array, an example is provided in the code formatted as double[][] noise = generateNoise(rand);
You can then call the drawNoise method or the drawFour method, the first drawing a single copy of the noise texture provided, the second drawing four copies tiled together to 
test if your texture is seamless. The method generateNoise creates a completely random texture, with every pixel's value being determined on its own. If you want an alternate
pattern, try genereateVoronoi, generateCells, generateWaves, or generateGradient. Respectively, they generate patterns of darkening in a radius around certain points,
darkening in a square around certain points, alternating from white to black with controllable frequency, and making a gradient. Each of these have their parameters explained 
a method comment.

There are then various methods you can call to modify your new noise 2D array. The simplest of which is "smooth". The smooth method accepts a 2D array of doubles (your image)
and an integer, which determines the amount of smoothing you want to see. The smooth method is made obselete by the makeTrueSeamless method, which does the same operation but
also loops over edges to reduce artifacting and make random noise textures blend with each other over borders of the image. (If you can't tell by now, I have some serious 
cleanup and QOL improvements to do. This is nowhere near a finished state.) There are math methods for every simple arithmetic operation, which each accept two 2D arrays of 
values, where the first one is operated on by the second. The remap method is necessary to preserve detail after smoothing an image, it takes no parameters besides a noise
texture, and outputs a version with every value in the image going between 0 and 1, even if the range had previously been restricted due to smoothing or another operation.
The colorCutoff method makes all pixel values above or below two respective thresholds either pure white or pure black. When combined with the extremaRemap method, which 
remaps all pure white or pure black values to their closest non 1 or 0 counterparts in the given image, it can be used to highlight the contrast over a specific range of 
values (IE: remap(extremaRemap(colorCutoff(makeTrueSeamless(generateNoise(rand), 20), 0.5, 0.75)))). 
