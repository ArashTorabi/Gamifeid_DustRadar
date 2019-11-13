package edu.teco.gamifieddustradar.blebridge;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import edu.teco.gamifieddustradar.GameStartActivity;
import edu.teco.gamifieddustradar.data.DataService;


/**
 * Handler fragment for an established BLE connection. Handles a single device
 */
public class BLEBridgeHandler extends Fragment {

    private static final String TAG = BLEBridgeHandler.class.getSimpleName();

    // parameters
    private static final String ARG_DEVICEADDRESS = "param_deviceaddress";

    private String deviceAddress;

    // private members
    public static boolean isTransmitting = true;
    // constructors
    /**
     * Empty constructor. Use static newInstance(...) instead
     */
    public BLEBridgeHandler() {
    }

    /**
     * @param deviceAddress BLE device address of the current connection
     * @return Instance of BLEBridgeHandler
     */
    public static BLEBridgeHandler newInstance(String deviceAddress) {
        BLEBridgeHandler fragment = new BLEBridgeHandler();
        Bundle args = new Bundle();
        args.putString(ARG_DEVICEADDRESS, deviceAddress);
        fragment.setArguments(args);
        return fragment;
    }
    // event handlers
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceAddress = getArguments().getString(ARG_DEVICEADDRESS);
        }
        //Start Game
        Intent gameIntent = new Intent(BLEBridgeHandler.this.getActivity(), GameStartActivity.class);
        gameIntent.putExtra(DataService.EXTRA_DATASERVICE_ADDRESS,deviceAddress);
        BLEBridgeHandler.this.getActivity().startActivity(gameIntent);
    }
}
