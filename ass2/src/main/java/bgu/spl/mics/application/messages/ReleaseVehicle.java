package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

public class ReleaseVehicle implements Event<Boolean> {
    private DeliveryVehicle vehicleToRelease;
    public ReleaseVehicle(DeliveryVehicle vehicleToRelease){
        this.vehicleToRelease=vehicleToRelease;
    }
    public DeliveryVehicle getVehicleToRelease(){
        return vehicleToRelease;
    }
}
