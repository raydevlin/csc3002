package shared.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.nio.Buffer;
import java.util.ArrayList;

public class FileReader {

    public FileReader() {

    }

    public int[][] parseFile(String path) {
        File file = new File(path);
        return parseFile(file);
    }

    public int[][] parseFile(File file) {
        int[][] data = null;
        String fileType = file.getAbsolutePath().toLowerCase().substring(file.getAbsolutePath().lastIndexOf('.')+1);

        if(fileType.equals("txt") || fileType.equals("csv")) {
            try {
                BufferedReader in = new BufferedReader(new java.io.FileReader(file));
                ArrayList<String> lines = new ArrayList<>();
                String line = in.readLine();
                while(line != null) {
                    if(line.isEmpty()) continue;
                    lines.add(line);
                    line = in.readLine();
                }

                int width = 0;
                int height = lines.size();
                for(String ln : lines) {
                    String[] constituents = ln.split(",");
                    if(constituents.length > width) width = constituents.length;
                }

                data = new int[height][width];

                for(int lnIndex = 0; lnIndex < lines.size(); lnIndex++) {
                    String ln = lines.get(lnIndex);
                    String[] constituents = ln.split(",");
                    for(int constIndex = 0; constIndex < constituents.length; constIndex++) {
                        data[lnIndex][constIndex] = Integer.parseInt(constituents[constIndex]);
                    }
                }
                System.out.println(lines);
            }
            catch (Exception e) { e.printStackTrace(); }
        }
        if(fileType.equals("png")) {
            try {
                BufferedImage image = ImageIO.read(file);
                int width = image.getWidth();
                int height = image.getHeight();

                BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = copy.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, copy.getWidth(), copy.getHeight());
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();

                int[] flattenedImageData = new int[width*height * 3];
                data = new int[height][width];
                copy.getData().getPixels(0,0, width, height, flattenedImageData);

                Color color = null;
                for(int i = 0; i < flattenedImageData.length/3; i++) {

                    color = new Color(flattenedImageData[i*3],flattenedImageData[(i*3)+1],flattenedImageData[(i*3)+2]);
                    if(color.equals(Color.BLACK)) data[i/width][i%width] = 1;
                }

            } catch (Exception e) { e.printStackTrace(); }
        }

        return data;
    }

}