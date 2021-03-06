package me.StevenLawson.BukkitTelnetClient;

import java.util.List;
import org.w3c.dom.*;

public class PlayerCommandEntry
{
    private final String name;
    private final String format;

    public PlayerCommandEntry(String name, String format)
    {
        this.name = name;
        this.format = format;
    }

    public String getFormat()
    {
        return format;
    }

    public String getName()
    {
        return name;
    }

    public static Element listToXML(final List<PlayerCommandEntry> playerCommands, final Document doc)
    {
        final Element plcElement = doc.createElement("playerCommands");

        for (final PlayerCommandEntry command : playerCommands)
        {
            final Element commandElement = doc.createElement("playerCommand");
            plcElement.appendChild(commandElement);

            final Element commandName = doc.createElement("name");
            commandName.appendChild(doc.createTextNode(command.getName()));
            commandElement.appendChild(commandName);

            final Element commandFormat = doc.createElement("format");
            commandFormat.appendChild(doc.createTextNode(command.getFormat()));
            commandElement.appendChild(commandFormat);
        }

        return plcElement;
    }

    public static boolean xmlToList(final List<PlayerCommandEntry> playerCommands, final Document doc)
    {
        NodeList playerCommandNodes = doc.getDocumentElement().getElementsByTagName("playerCommands");
        if (playerCommandNodes.getLength() < 1)
        {
            return false;
        }
        playerCommandNodes = playerCommandNodes.item(0).getChildNodes();

        playerCommands.clear();

        for (int i = 0; i < playerCommandNodes.getLength(); i++)
        {
            final Node node = playerCommandNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                final Element element = (Element) node;

                final PlayerCommandEntry command = new PlayerCommandEntry(
                        element.getElementsByTagName("name").item(0).getTextContent(),
                        element.getElementsByTagName("format").item(0).getTextContent()
                );

                playerCommands.add(command);
            }
        }

        return true;
    }
}
