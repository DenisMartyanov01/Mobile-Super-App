package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.myapplication.Notifications.NotificationData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NotificationDataTest {

    @Test
    public void testNotificationDataCreation() {
        NotificationData data = new NotificationData(
                1,
                "Test Title",
                "Test Message",
                System.currentTimeMillis() + 3600000
        );

        assertEquals(1, data.getId());
        assertEquals("Test Title", data.getTitle());
        assertEquals("Test Message", data.getMessage());
        assertTrue(data.getTimeInMillis() > System.currentTimeMillis());
    }

    @Test
    public void testIsTimePassed() {
        // Прошлое время
        NotificationData pastNotification = new NotificationData(
                1, "Test", "Test", System.currentTimeMillis() - 1000
        );
        assertTrue(pastNotification.isTimePassed());

        // Будущее время
        NotificationData futureNotification = new NotificationData(
                2, "Test", "Test", System.currentTimeMillis() + 1000
        );
        assertFalse(futureNotification.isTimePassed());
    }

    @Test
    public void testRemainingTimeFormat() {
        NotificationData data = new NotificationData(
                1, "Test", "Test", System.currentTimeMillis() + 65000
        );

        String remaining = data.getRemainingTime();
        assertNotNull(remaining);
        assertTrue(remaining.contains("мин") || remaining.contains("сек"));
    }
}
