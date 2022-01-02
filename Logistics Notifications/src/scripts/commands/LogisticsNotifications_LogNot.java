package scripts.commands;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import scripts.campaign.LogisticsNotifications_NotificationScript;

/**
 * Author: SafariJohn
 */
public class LogisticsNotifications_LogNot implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context)
    {
        String m = new LogisticsNotifications_NotificationScript().displayConsole(true, true);

        Console.showMessage(m);

        return CommandResult.SUCCESS;
    }
}
