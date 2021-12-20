package me.lucko.networkinterceptor.velocity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.velocitypowered.api.plugin.PluginContainer;

import me.lucko.networkinterceptor.plugin.KeepPlugins;
import me.lucko.networkinterceptor.plugin.PluginOptions;

public class VelocityPluginOptions extends PluginOptions<PluginContainer> {
    private final VelocityNetworkInterceptor owner;
    private final Set<PluginContainer> plugins = new HashSet<>();

    public VelocityPluginOptions(VelocityNetworkInterceptor owner, KeepPlugins keepType, boolean allowNonPlugin,
            Set<String> plugins) {
        this(owner, keepType, allowNonPlugin, plugins, true);
    }

    public VelocityPluginOptions(VelocityNetworkInterceptor owner, KeepPlugins keepType, boolean allowNonPlugin,
            Set<String> plugins, boolean trust) {
        super(keepType, allowNonPlugin, plugins, trust);
        this.owner = owner;
    }

    @Override
    protected boolean attemptAddPlugin(String name) {
        Optional<PluginContainer> plugin = owner.getServer().getPluginManager().getPlugin(name);
        if (plugin == null || !plugin.isPresent()) {
            return false;
        }
        this.plugins.add(plugin.get());
        return true;
    }

    @Override
    public boolean isTrusted(PluginContainer plugin) {
        return plugins.contains(plugin) == trust; // if trust is true, plugin must be listed; if trust is false, plugin
                                                  // must not be listed
    }

}
