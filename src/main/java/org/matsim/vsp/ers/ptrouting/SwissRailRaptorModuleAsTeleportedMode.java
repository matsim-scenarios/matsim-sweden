package org.matsim.vsp.ers.ptrouting;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.*;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.RoutingModule;

import java.util.Iterator;

public class SwissRailRaptorModuleAsTeleportedMode extends AbstractModule {
    public SwissRailRaptorModuleAsTeleportedMode() {
    }

    public void install() {
        this.bind(SwissRailRaptor.class).toProvider(SwissRailRaptorFactory.class);
        Iterator var1 = this.getConfig().transit().getTransitModes().iterator();

        while (var1.hasNext()) {
            String mode = (String) var1.next();
            this.addRoutingModuleBinding(mode).toProvider(SwissRailRaptorRoutingModuleProvider.class);
        }

        this.addRoutingModuleBinding("transit_walk").to(Key.get(RoutingModule.class, Names.named("walk")));
        this.bind(RaptorParametersForPerson.class).to(DefaultRaptorParametersForPerson.class);
        SwissRailRaptorConfigGroup srrConfig = (SwissRailRaptorConfigGroup) ConfigUtils.addOrGetModule(this.getConfig(), SwissRailRaptorConfigGroup.class);
        if (srrConfig.isUseRangeQuery()) {
            this.bind(RaptorRouteSelector.class).to(ConfigurableRaptorRouteSelector.class);
        } else {
            this.bind(RaptorRouteSelector.class).to(LeastCostRaptorRouteSelector.class);
        }

        if (srrConfig.isUseIntermodalAccessEgress()) {
            this.bind(MainModeIdentifier.class).to(IntermodalAwareRouterModeIdentifier.class);
        }
    }


}
