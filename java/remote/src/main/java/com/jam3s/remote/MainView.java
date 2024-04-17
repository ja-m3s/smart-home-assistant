import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Main")
@Route(value = "")
public class MainView extends HorizontalLayout {

    private VerticalLayout lightBulbDisplayVLayout; // Reference to the paragraph
    private ScheduledExecutorService executorService;

    public MainView() {
        executorService = Executors.newScheduledThreadPool(1);

        setMargin(true);

        // Schedule the task to execute every 1 second
        executorService.scheduleAtFixedRate(this::handleGetLightBulbStatus, 0, 1, TimeUnit.SECONDS);
    }

    private void handleGetLightBulbStatus() {
        Notification.show("Getting light bulb status...");
        if (lightBulbDisplayVLayout == null) {
            lightBulbDisplayVLayout = new VerticalLayout(); // Use VerticalLayout to stack components vertically
            add(lightBulbDisplayVLayout);
        }
        lightBulbDisplayVLayout.removeAll(); // Remove existing content before adding new content
    
        // Iterate over the entries in the HashMap
        RemoteApplication.getLightBulbStatus().forEach((key, value) -> {
            NativeLabel keyLabel = new NativeLabel(key); // Create a Label with the key
            TextArea valueTextArea = new TextArea(); // Create a TextArea to display the message
            valueTextArea.setValue(value.toString()); // Set the value of the TextArea
            valueTextArea.setReadOnly(true); // Make the TextArea read-only
            Button toggleState = new Button("Toggle On/Off");
            toggleState.addClickListener(d -> handleToggleState(key));
            // Add the Label and TextArea and Button to a HorizontalLayout
            HorizontalLayout entryLayout = new HorizontalLayout(keyLabel, valueTextArea, toggleState);
            lightBulbDisplayVLayout.add(entryLayout); // Add the HorizontalLayout to the VerticalLayout
        });
    }

    private void handleToggleState(String key) {
        Notification.show("Toggling light bulb...");
        try {
            RemoteApplication.sendMessage(RemoteApplication.createTurnOnMessage(key));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Shutdown the executor service when the UI is detached
        executorService.shutdown();
    }
}
