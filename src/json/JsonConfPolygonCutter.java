package json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class JsonConfPolygonCutter {

    private static final String configurationFile = "C:\\Shared Folder\\example1_labels.json";
    private static final String imageFilePathString = "C:\\Shared Folder\\20180309_101543.jpg";
    private static final String segmentedDir = System.getProperty("user.dir") + "\\Segmented";
//    private static final String occupiedDir = segmentedDir + "\\Occupied";
//    private static final String emptyDir = segmentedDir + "\\Empty";

    public static void main(String[] args) throws ParserConfigurationException, IOException, ParseException {
        JSONParser parser = new JSONParser();
        File jsonConfiguration = new File(configurationFile);
        BufferedImage in = ImageIO.read(new File(imageFilePathString));
        Object obj = parser.parse(new FileReader(jsonConfiguration));
        JSONArray jsonArray = (JSONArray) obj;
        JSONObject object = (JSONObject) jsonArray.get(0);
        JSONArray annotationsArray = (JSONArray) object.get("annotations");
        int size = annotationsArray.size();
        int id = 0;

        for(int i = 0; i < size; i++) {
            JSONObject annotationsJSONObj = (JSONObject) annotationsArray.get(i);
            String[] xn = ((String) annotationsJSONObj.get("xn")).split(";");
            String[] yn = ((String) annotationsJSONObj.get("yn")).split(";");
            Polygon currentParkingLotPolygon = new Polygon();

            System.out.println("Xn="+Arrays.toString(xn));
            System.out.println("Yn="+Arrays.toString(yn));

            if(xn.length != yn.length) {
                throw new RuntimeException("x and y arrays are of different sizes!");
            }

            int numOfVertexes = xn.length;
            double[] xnArr = new double[numOfVertexes];
            double[] ynArr = new double[numOfVertexes];
            // We need this stuff to rotate the polygon:
            double x0 = Double.MAX_VALUE;
            double y0 = Double.MAX_VALUE;
            double x1 = Double.MAX_VALUE; // first smallest x
            double y1 = Double.MAX_VALUE;
            double x2 = Double.MAX_VALUE; // second smallest x
            double y2 = Double.MAX_VALUE;
            boolean rotationIsNecessary = true;

            for (int j = 0; j < numOfVertexes; j++) {
                double currentX = Double.parseDouble(xn[j]);
                double currentY = Double.parseDouble(yn[j]);
                if(x1 > currentX) {
                    x1 = currentX;
                    y1 = currentY;
                }
                xnArr[j] = currentX;
                ynArr[j] = currentY;
                int x = (int) Math.round(currentX);
                int y = (int) Math.round(currentY);
                currentParkingLotPolygon.addPoint(x, y);
            }

            for(int j = 0; j < numOfVertexes; j++) {
                double currentX = xnArr[j];
                if(currentX > x1 && currentX < x2) {
                    x2 = currentX;
                    y2 = ynArr[j];
                }
            }
            // Second point was not found - it means leftmost polygon side is already on its place, no need to rotate
            if(Double.compare(x2, Double.MAX_VALUE) == 0) {
                rotationIsNecessary = false;
            }

            Rectangle bounds = currentParkingLotPolygon.getBounds();
            BufferedImage out = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
            currentParkingLotPolygon.translate(-bounds.x, -bounds.y);
            Graphics2D g = out.createGraphics();
            g.setClip(currentParkingLotPolygon);

            // TODO: reshape it!
//            g.rotate(Math.toRadians(degrees));
            g.drawImage(in, -bounds.x, -bounds.y, null);
            String postfix = File.separator + ++id + ".png";
            File extImageFile = new File(segmentedDir + postfix);
            extImageFile.mkdirs();
            ImageIO.write(out, "png", extImageFile);
        }
    }
}
