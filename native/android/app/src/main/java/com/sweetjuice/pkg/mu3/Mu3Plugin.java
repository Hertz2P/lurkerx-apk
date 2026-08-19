package com.sweetjuice.pkg.mu3;

import android.content.Context;
import com.sweetjuice.core.UIManager;
import com.sweetjuice.plugin.SweetJuicePlugin;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;

public class Mu3Plugin implements SweetJuicePlugin {

    private UIManager mUIManager;
    private Mu3CardWidgetFactory mCardFactory;
    private Mu3BoxWidgetFactory mBoxFactory;

    @Override
    public String getDomain() {
        return "mu3";
    }

    @Override
    public void onAttach(Context context) {
        // no-op
    }

    @Override
    public void onWidgetFactoriesRegistered(UIManager uiManager) {
        this.mUIManager = uiManager;
        if (mCardFactory != null) mCardFactory.setUIManager(uiManager);
        if (mBoxFactory != null) mBoxFactory.setUIManager(uiManager);
    }

    @Override
    public String handleAction(String action, String jsonArgsPayload) {
        return "{}";
    }

    @Override
    public SweetJuiceWidgetFactory[] getWidgetFactories() {
        mCardFactory = new Mu3CardWidgetFactory();
        mCardFactory.setUIManager(mUIManager);

        mBoxFactory = new Mu3BoxWidgetFactory();
        mBoxFactory.setUIManager(mUIManager);

        return new SweetJuiceWidgetFactory[] {
            new Mu3TextWidgetFactory(),
            new Mu3ButtonWidgetFactory(),
            new Mu3TextButtonWidgetFactory(),
            new Mu3OutlinedButtonWidgetFactory(),
            new Mu3TonalButtonWidgetFactory(),
            new Mu3ElevatedButtonWidgetFactory(),
            new Mu3IconButtonWidgetFactory(),
            new Mu3FabWidgetFactory(),
            new Mu3ExtendedFabWidgetFactory(),
            new Mu3SegmentedButtonWidgetFactory(),
            new Mu3ButtonGroupWidgetFactory(),
            new Mu3TextFieldWidgetFactory(),
            new Mu3ImageWidgetFactory(),
            new Mu3VideoWidgetFactory(),
            mCardFactory,
            mBoxFactory,
            new Mu3SpacerWidgetFactory(),
            new Mu3IconOutlinedWidgetFactory(),
            new Mu3TopAppBarWidgetFactory(),
            new Mu3BottomAppBarWidgetFactory(),
            new Mu3NavigationBarWidgetFactory(),
            new Mu3NavigationRailWidgetFactory(),
            new Mu3SearchBarWidgetFactory(),
            new Mu3TabsWidgetFactory(),
            new Mu3ToolbarWidgetFactory(),
            new Mu3DialogWidgetFactory()
        };
    }
}
