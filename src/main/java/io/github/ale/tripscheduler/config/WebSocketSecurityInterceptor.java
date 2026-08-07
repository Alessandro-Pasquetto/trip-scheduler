package io.github.ale.tripscheduler.config;

import io.github.ale.tripscheduler.repository.TripPlanUserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    private final TripPlanUserRepository tripPlanUserRepository;

    public WebSocketSecurityInterceptor(TripPlanUserRepository tripPlanUserRepository) {
        this.tripPlanUserRepository = tripPlanUserRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            if (destination != null && destination.startsWith("/topic/trip-plan/")) {

                Long tripPlanId = Long.parseLong(
                        destination.replace(
                                "/topic/trip-plan/",
                                ""
                        )
                );

                boolean isMember = true;

                // todo: check if the user is a member of the trip plan

                if (!isMember) {
                    throw new SecurityException(
                            "User is not a member of this trip plan"
                    );
                }
            }
        }

        return message;
    }
}