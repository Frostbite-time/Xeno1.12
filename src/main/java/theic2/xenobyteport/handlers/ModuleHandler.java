package theic2.xenobyteport.handlers;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import theic2.xenobyteport.ModulesList;
import theic2.xenobyteport.api.gui.WidgetMessage;
import theic2.xenobyteport.api.gui.WidgetMode;
import theic2.xenobyteport.api.module.CheatModule;
import theic2.xenobyteport.api.module.PerformMode;
import theic2.xenobyteport.api.module.PerformSource;
import theic2.xenobyteport.gui.click.elements.Button;
import theic2.xenobyteport.modules.Widgets;
import theic2.xenobyteport.modules.XenoGui;
import theic2.xenobyteport.utils.Config;
import theic2.xenobyteport.utils.EventRegisterer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModuleHandler {

    private List<CheatModule> modulesList, workingList, enabledList;

    public ModuleHandler() {
        modulesList = new ModulesList();
        enabledList = new CopyOnWriteArrayList<CheatModule>();
        workingList = allModules().filter(CheatModule::isWorking).collect(Collectors.toList());
        allModules().forEach(m -> m.handleInit(this));
        new Config(this);
        enabledList = workingModules().filter(m -> m.cfgState || m.getMode() == PerformMode.ON_START).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        allModules().forEach(CheatModule::onPostInit);
        new PacketHandler(this, Minecraft.getMinecraft().getConnection());
        new EventHandler(this);
    }

    public Stream<CheatModule> allModules() {
        return modulesList.stream();
    }

    public Stream<CheatModule> enabledModules() {
        return enabledList.stream();
    }

    public Stream<CheatModule> workingModules() {
        return workingList.stream();
    }

    public Stream<CheatModule> categoryedModules() {
        return workingModules().filter(CheatModule::hasCategory);
    }

    public XenoGui xenoGui() {
        return (XenoGui) getModuleByClass(XenoGui.class);
    }

    public Widgets widgets() {
        return (Widgets) getModuleByClass(Widgets.class);
    }

    public CheatModule getModuleByID(String id) {
        return moduleGetter(m -> m.getID().equals(id));
    }

    public CheatModule getModuleByName(String name) {
        return moduleGetter(m -> m.getName().equals(name));
    }

    public CheatModule getModuleByClass(Class<? extends CheatModule> clazz) {
        return moduleGetter(m -> m.getClass().equals(clazz));
    }

    public CheatModule moduleGetter(Predicate<CheatModule> predicate) {
        Optional<CheatModule> module = allModules().filter(predicate).findFirst();
        return module.isPresent() ? module.get() : null;
    }

    public void perform(CheatModule module) {
        perform(module, null);
    }

    public boolean isEnabled(CheatModule module) {
        return module != null && enabledList.contains(module);
    }

    public void enable(CheatModule module) {
        if (!isEnabled(module)) {
            enabledList.add(module);
            module.cfgState = true;
            module.setLastCounter();
            if (module.provideStateEvents()) {
                module.onEnabled();
                workingModules().forEach(m -> m.onModuleEnabled(module));
            }
            if (module.provideForgeEvents()) {
                EventRegisterer.register(module);
            }
        }
    }

    public void disable(CheatModule module) {
        if (isEnabled(module)) {
            enabledList.remove(module);
            module.cfgState = false;
            if (module.provideStateEvents()) {
                module.onDisabled();
                workingModules().forEach(m -> m.onModuleDisabled(module));
            }
            if (module.provideForgeEvents()) {
                EventRegisterer.unregister(module);
            }
        }
    }

    public void bind(CheatModule module, Button button, int key) {
        CheatModule conflicted = moduleGetter(m -> m.hasKeyBind() && m.getKeyBind() == key);
        if (conflicted != null && !conflicted.equals(module)) {
            widgets().widgetMessage(new WidgetMessage("Кейбинд [" + Keyboard.getKeyName(key) + "] конфликтует с " + (conflicted.hasCategory() ? conflicted.getCategory() + "/" : "") + conflicted, WidgetMode.FAIL));
        } else {
            if (module.getKeyBind() == key) {
                if (!module.equals(xenoGui())) {
                    module.resetKeyBind();
                    button.buttonValue(null);
                    if (module.provideStateEvents()) {
                        workingModules().forEach(m -> m.onModuleUnBinded(module));
                    }
                }
            } else {
                module.setKeyBind(key);
                button.buttonValue(module.getKeyName());
                if (module.provideStateEvents()) {
                    workingModules().forEach(m -> m.onModuleBinded(module));
                }
            }
        }
    }

    public void perform(CheatModule module, Button button) {
        PerformMode mode = module.getMode();
        if (mode == PerformMode.TOGGLE) {
            if (isEnabled(module)) {
                disable(module);
            } else {
                enable(module);
            }
            boolean enabled = isEnabled(module);
            if (button != null) {
                button.setSelected(enabled);
            }
            if (module.allowStateMessages()) {
                widgets().widgetMessage(new WidgetMessage(module, enabled ? "ON" : "OFF", enabled ? WidgetMode.SUCCESS : WidgetMode.FAIL));
            }
        } else if (mode == PerformMode.SINGLE) {
            if (module.allowStateMessages()) {
                widgets().widgetMessage(new WidgetMessage(module, "выполнен", WidgetMode.INFO));
            }
        }
        module.onPerform(button == null ? PerformSource.KEY : PerformSource.BUTTON);
    }

}