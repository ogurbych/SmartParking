package xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class XmlConfPolygonCutter {

    private static final String configurationFile = "C:\\Shared Folder\\PKLot\\PKLot\\PUCPR\\Sunny\\2012-09-11\\2012-09-11_15_16_58.xml";
    private static final String imageFilePathString = "C:\\Shared Folder\\PKLot\\PKLot\\PUCPR\\Sunny\\2012-09-11\\2012-09-11_15_16_58.jpg";
    private static final String segmentedDir = System.getProperty("user.dir")+"\\Segmented";
    private static final String occupiedDir = segmentedDir+"\\Occupied";
    private static final String emptyDir = segmentedDir+"\\Empty";

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        File xmlConfiguration = new File(configurationFile);
        BufferedImage in = ImageIO.read(new File(imageFilePathString));
        File occupiedOutDir = new File(occupiedDir);
        occupiedOutDir.mkdirs();
        File emptyOutDir = new File(emptyDir);
        emptyOutDir.mkdirs();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlConfiguration);
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("space");
        int amountOfNodes = nList.getLength();

        System.out.println("Total amount nodes: " + amountOfNodes);

        for(int i = 0; i < amountOfNodes; i++) {
            Node node = nList.item(i);

            System.out.println("Current node name: " + node.getNodeName());

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                boolean isOccupied = element.getAttribute("occupied").equals("1");
                String id = element.getAttribute("id");
                System.out.printf("id=%s, is occupied=%b\n", id, isOccupied);

                // Build parking lot polygon:
                NodeList pointList = element.getElementsByTagName("point");
                int pointsAmount = pointList.getLength();
                Polygon currentParkingLotPolygon = new Polygon();

                System.out.println("Amount of points: " + pointsAmount);

                for(int j = 0; j < pointsAmount; j++) {
                    Element point = (Element) pointList.item(j);
                    int x = Integer.parseInt(point.getAttribute("x"));
                    int y = Integer.parseInt(point.getAttribute("y"));
                    currentParkingLotPolygon.addPoint(x, y);
                }

                Rectangle bounds = currentParkingLotPolygon.getBounds();
                BufferedImage out = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
                currentParkingLotPolygon.translate(-bounds.x, -bounds.y);
                Graphics2D g = out.createGraphics();
                g.setClip(currentParkingLotPolygon);
                g.drawImage(in, -bounds.x, -bounds.y, null);
                String postfix = File.separator + id + ".png";
                String targetFilePath = isOccupied ? occupiedDir + postfix : emptyDir + postfix;
                File extImageFile = new File(targetFilePath);
                ImageIO.write(out, "png", extImageFile);
            }
        }
    }
}
