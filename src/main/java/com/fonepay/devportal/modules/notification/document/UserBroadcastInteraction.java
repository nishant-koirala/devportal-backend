package com.fonepay.devportal.modules.notification.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "staff_broadcast_interactions")
@CompoundIndexes({
        @CompoundIndex(name = "user_broadcast_unique_idx", def = "{'user_id': 1, 'broadcast_id': 1}", unique = true),
        @CompoundIndex(name = "user_read_idx", def = "{'user_id': 1, 'is_read': 1}")
})
public class UserBroadcastInteraction {

    @Id
    private String id;

    @Indexed
    @Field("user_id")
    private String userId;

    @Indexed
    @Field("broadcast_id")
    private String broadcastId;

    @Builder.Default
    @Field("is_read")
    private boolean isRead = false;

    @Field("read_at")
    private Instant readAt;

    @Builder.Default
    @Field("is_dismissed")
    private boolean isDismissed = false;

    @Field("dismissed_at")
    private Instant dismissedAt;

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;
}
