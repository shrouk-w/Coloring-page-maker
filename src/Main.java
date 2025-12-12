import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static List<int[]> colorPalette = new ArrayList<>();

    public static void main(String[] args) {
        try {

            File input = new File(args[0]);
            BufferedImage image = ImageIO.read(input);

            Map<Integer, Integer> colorCount = new HashMap<>();
//dupa dupa
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);

                    //int alpha = (argb >> 24) & 0xFF;
                    int red = (argb >> 16) & 0xC0; // 11100000 in hex
                    int green = (argb >> 8) & 0xC0;
                    int blue = argb & 0xC0;

                    int newArgb = (red << 16) | (green << 8) | blue;

                    colorCount.put(newArgb, colorCount.getOrDefault(newArgb, 0) + 1);
                }
            }  //standardize all bytes to only 3 bites precision

            colorPalette = colorCount.entrySet().stream()
                    .sorted(
                            (e1, e2)->{
                                return e2.getValue().compareTo(e1.getValue());
                            }
                    )
                    .limit(Integer.parseInt(args[1]))
                    .map(Map.Entry::getKey)
                    .map((e1)->{
                        return new int[]{(e1>>16)&0xFF, (e1>>8)&0xFF, (e1)&0xFF};
                    })
                    .collect(Collectors.toList());
            //get top colors from the standardized picture

            colorPalette.add(new int[]{0,0,0});

            //always add black
//tralalala
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);

                    int red = (argb >> 16) & 0xFF;
                    int green = (argb >> 8) & 0xFF;
                    int blue = argb & 0xFF;

                    int newArgb[] = findClosestColor(red, green, blue);
                    int newColor = (newArgb[0] << 16) | (newArgb[1] << 8) | newArgb[2];

                    image.setRGB(x,y,newColor);
                }
            }
            //set all the colors to the closest from the top color list

            for (int y = 0; y < image.getHeight() - 1; y += 2) {
                for (int x = 0; x < image.getWidth() - 1; x += 2) {
                    int leftup = image.getRGB(x, y + 1);
                    int rightup = image.getRGB(x + 1, y + 1);
                    int leftdown = image.getRGB(x, y);
                    int rightdown = image.getRGB(x + 1, y);


                    boolean allDifferent = !(leftup == rightup && leftdown == rightdown && leftup == rightdown);


                    boolean noneBlack = leftup != Color.BLACK.getRGB() &&
                            rightup != Color.BLACK.getRGB() &&
                            leftdown != Color.BLACK.getRGB() &&
                            rightdown != Color.BLACK.getRGB();

                    if (allDifferent && noneBlack) {
                        image.setRGB(x, y, Color.BLACK.getRGB());
                        image.setRGB(x + 1, y, Color.BLACK.getRGB());
                        image.setRGB(x, y + 1, Color.BLACK.getRGB());
                        image.setRGB(x + 1, y + 1, Color.BLACK.getRGB());
                    }
                }
            }

            //go through the picture check 2x2 pixels and if there is any difference change all to black
            //dont change if any of them is black already


            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int currentPixel = image.getRGB(x, y);


                    int red = (currentPixel >> 16) & 0xFF;
                    int green = (currentPixel >> 8) & 0xFF;
                    int blue = currentPixel & 0xFF;


                    double luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue;

                    //for "close to black" (adjust the value if needed)

                    if (luminance < Integer.parseInt(args[2])) {
                        image.setRGB(x, y, Color.BLACK.getRGB());
                    } else {
                        image.setRGB(x, y, Color.WHITE.getRGB());
                    }
                }
            }
            //change pixels that are almost black to black all the other to white

            File output = new File("output.png");
            ImageIO.write(image, "png", output);

            System.out.println("Image processed and saved as output.png");
        } catch (IOException e) {
            System.err.println("Error processing the image: " + e.getMessage());
        }
    }

    private static int[] findClosestColor(int red, int green, int blue) {
        int[] closestColor = null;
        double closestDistance = Double.MAX_VALUE;

        for (int[] color : colorPalette) {
            double distance = Math.sqrt(
                    Math.pow(red - color[0], 2) +
                            Math.pow(green - color[1], 2) +
                            Math.pow(blue - color[2], 2)
            );
            if (distance < closestDistance) {
                closestDistance = distance;
                closestColor = color;
            }
        }

        return closestColor;
    }



}