package org.matsim.vsp.ers.ptrouting;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.*;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.RoutingModule;

public class SwissRailRaptorModuleAsTeleportedMode extends AbstractModule {

    @Override
    public void install() {

        bind(SwissRailRaptor.class).toProvider(SwissRailRaptorFactory.class);

        for (String mode : getConfig().transit().getTransitModes()) {
            addRoutingModuleBinding(mode).toProvider(SwissRailRaptorRoutingModuleProvider.class);
        }
        addRoutingModuleBinding(TransportMode.transit_walk).to(Key.get(RoutingModule.class, Names.named(TransportMode.walk)));
        bind(RaptorParametersForPerson.class).to(DefaultRaptorParametersForPerson.class);

        SwissRailRaptorConfigGroup srrConfig = ConfigUtils.addOrGetModule(getConfig(), SwissRailRaptorConfigGroup.class);

        if (srrConfig.isUseRangeQuery()) {
            bind(RaptorRouteSelector.class).to(ConfigurableRaptorRouteSelector.class);
        } else {
            bind(RaptorRouteSelector.class).to(LeastCostRaptorRouteSelector.class); // just a simple default in case it ever gets used.
        }

        if (srrConfig.isUseIntermodalAccessEgress()) {
            bind(MainModeIdentifier.class).to(IntermodalAwareRouterModeIdentifier.class);
        }
        bind(RaptorIntermodalAccessEgress.class).to(DefaultRaptorIntermodalAccessEgress.class);

        bind( RaptorStopFinder.class ).to( DefaultRaptorStopFinder.class );
    }


}

