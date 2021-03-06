package me.StevenLawson.BukkitTelnetClient;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class BTC_ConfigLoader
{
    private static final String SETTINGS_FILE = "settings.xml";

    private final List<PlayerCommandEntry> playerCommands = new ArrayList<>();
    private final Set<ServerEntry> servers = new HashSet<>();

    public BTC_ConfigLoader()
    {
    }

    public boolean load(boolean verbose)
    {
        File settings = new File("settings.xml");

        if (!settings.exists())
        {
            if (extractFileFromJar("/" + SETTINGS_FILE, SETTINGS_FILE))
            {
                if (verbose)
                {
                    System.out.println("Copied default " + SETTINGS_FILE + ".");
                }
            }
        }

        if (settings.exists())
        {
            boolean loadError = loadXML(settings);

            final List<ServerEntry> oldServers = importOldConfig();
            this.servers.addAll(oldServers);

            generateXML(settings);

            if (verbose)
            {
                if (loadError)
                {
                    System.out.println("Settings loaded with errors.");
                }
                else
                {
                    System.out.println("Settings loaded.");
                }
            }

            return true;
        }
        else
        {
            if (verbose)
            {
                System.out.println("Can't load " + SETTINGS_FILE + ".");
            }
        }

        return false;
    }

    public boolean save()
    {
        return generateXML(new File(SETTINGS_FILE));
    }

    public List<PlayerCommandEntry> getCommands()
    {
        return this.playerCommands;
    }

    public Set<ServerEntry> getServers()
    {
        return servers;
    }

    private boolean generateXML(final File file)
    {
        try
        {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            final Element rootElement = doc.createElement("configuration");
            doc.appendChild(rootElement);

            rootElement.appendChild(PlayerCommandEntry.listToXML(this.playerCommands, doc));
            rootElement.appendChild(ServerEntry.listToXML(this.servers, doc));

            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.transform(new DOMSource(doc), new StreamResult(file));

            return true;
        }
        catch (IllegalArgumentException | ParserConfigurationException | TransformerException | DOMException ex)
        {
            BukkitTelnetClient.LOGGER.log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private boolean loadXML(final File file)
    {
        boolean hadErrors = false;

        try
        {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            doc.getDocumentElement().normalize();

            if (!PlayerCommandEntry.xmlToList(this.playerCommands, doc))
            {
                System.out.println("Error loading playerCommands.");
                hadErrors = true;
            }

            if (!ServerEntry.xmlToList(this.servers, doc))
            {
                System.out.println("Error loading servers.");
                hadErrors = true;
            }
        }
        catch (IOException | ParserConfigurationException | SAXException ex)
        {
            hadErrors = true;

            BukkitTelnetClient.LOGGER.log(Level.SEVERE, null, ex);
        }

        return hadErrors;
    }

    private static boolean extractFileFromJar(final String resourceName, final String fileName)
    {
        final InputStream resource = BTC_ConfigLoader.class.getResourceAsStream(resourceName);
        if (resource != null)
        {
            final File destination = new File(fileName);
            try
            {
                FileUtils.copyInputStreamToFile(resource, destination);
                return true;
            }
            catch (IOException ex)
            {
                BukkitTelnetClient.LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        return false;
    }

    private static List<ServerEntry> importOldConfig()
    {
        final List<ServerEntry> oldServers = new ArrayList<>();

        try
        {
            final File file = new File("btc_servers.cfg");
            if (file.exists())
            {
                try (final BufferedReader in = new BufferedReader(new FileReader(file)))
                {
                    String line;
                    while ((line = in.readLine()) != null)
                    {
                        line = line.trim();
                        oldServers.add(new ServerEntry("legacy", line, false));
                    }
                }

                FileUtils.moveFile(file, new File("btc_servers.cfg.bak"));
            }
        }
        catch (IOException ex)
        {
            BukkitTelnetClient.LOGGER.log(Level.SEVERE, null, ex);
        }

        return oldServers;
    }
}
