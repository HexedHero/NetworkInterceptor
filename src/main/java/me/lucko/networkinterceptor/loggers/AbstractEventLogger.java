package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.common.Platform;
import net.md_5.bungee.api.plugin.Plugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public abstract class AbstractEventLogger<PLUGIN> implements EventLogger<PLUGIN> {
    private final boolean includeTraces;
    protected final Platform platform;

    protected AbstractEventLogger(boolean includeTraces, Platform platform) {
        this.includeTraces = includeTraces;
        this.platform = platform;
    }

    protected abstract Logger getLogger();

    @Override
    public void logAttempt(InterceptEvent<PLUGIN> event) {
        String host = event.getHost();

        StringBuilder sb = new StringBuilder("Intercepted connection to ").append(host);
        String origHost = event.getOriginalHost();
        if (origHost != null) {
            sb.append(" (").append(origHost).append(")");
        }
        appendPluginIfPossible(sb, event);
        sb.append("\n");

        // print stacktrace
        if (this.includeTraces && !event.isRepeatCall()) {
            Map<StackTraceElement, PLUGIN> map = event.getNonInternalStackTraceWithPlugins();
            for (StackTraceElement element : map.keySet()) {
                sb.append("\tat ").append(element);
                if (platform == Platform.BUKKIT) {
                    JavaPlugin providingPlugin = (JavaPlugin) map.get(element);
                    if (providingPlugin != null) {
                        sb.append(" [").append(providingPlugin.getName()).append(']');
                    }
                }
                sb.append("\n");
            }
        } else if (this.includeTraces) {
            sb.append("\tat (identical stack trace omitted)\n");
        }

        sb.setLength(sb.length() - 1);
        getLogger().info(sb.toString());
    }

    // TODO - create a PluginManager that has a #getName(PLUGIN) method
    // and has an implementation for different platforms
    // and use that here to get plugin name instead of coupling platform-specific
    // stuff in here
    private void appendPluginIfPossible(StringBuilder sb, InterceptEvent<PLUGIN> event) {
        PLUGIN trustedPlugin = event.getTrustedPlugin();
        PLUGIN blockedPlugin = event.getBlockedPlugin();
        PLUGIN target = null;
        if (trustedPlugin != null) {
            target = trustedPlugin;
            sb.append(" by trusted-plugin ");
        } else if (blockedPlugin != null) {
            target = blockedPlugin;
            sb.append(" by blocked-plugin ");
        } else {
            Set<PLUGIN> traced = event.getOrderedTracedPlugins();
            if (!traced.isEmpty()) {
                sb.append(" by plugin ");
                target = traced.iterator().next();
            }
        }
        if (target != null) {
            if (platform == Platform.BUKKIT) {
                sb.append(((JavaPlugin) target).getName());
            } else if (platform == Platform.BUNGEE) {
                sb.append(((Plugin) target).getDescription().getName());
            } else {
                // TODO - velocity?
                // TODO - other?
            }
        }
    }

    @Override
    public void logBlock(InterceptEvent<PLUGIN> event) {
        StringBuilder sb = new StringBuilder("Blocked connection to ");
        sb.append(event.getHost());
        String origHost = event.getOriginalHost();
        if (origHost != null) {
            sb.append(" (").append(origHost).append(")");
        }
        appendPluginIfPossible(sb, event);
        getLogger().info(sb.toString());
    }
}
