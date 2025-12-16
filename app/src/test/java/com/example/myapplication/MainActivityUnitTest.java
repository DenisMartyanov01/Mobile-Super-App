package com.example.myapplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.quality.Strictness;

import android.content.Context;
import android.app.AlarmManager;
import android.app.NotificationManager;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.myapplication.Notifications.MainActivity;

@RunWith(MockitoJUnitRunner.class)
public class MainActivityUnitTest {

    @Mock
    private Context mockContext;

    @Mock
    private AlarmManager mockAlarmManager;

    @Mock
    private NotificationManager mockNotificationManager;

    @Test
    public void testForAndroid13_OnlyNotifications() {
        Context mockContext = mock(Context.class);
        NotificationManager mockNotificationManager = mock(NotificationManager.class);
        int android13 = 33; // TIRAMISU

        when(mockContext.getSystemService(Context.NOTIFICATION_SERVICE))
                .thenReturn(mockNotificationManager);
        // НЕ мокаем AlarmManager, так как для Android 13 он не нужен

        when(mockNotificationManager.areNotificationsEnabled())
                .thenReturn(true);

        // Act
        boolean result = MainActivity.checkAllNotificationPermissions(mockContext, android13);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testForAndroid12_OnlyAlarms() {
        // Arrange
        Context mockContext = mock(Context.class);
        AlarmManager mockAlarmManager = mock(AlarmManager.class);
        int android12 = 31; // S

        when(mockContext.getSystemService(Context.ALARM_SERVICE))
                .thenReturn(mockAlarmManager);
        // НЕ мокаем NotificationManager

        when(mockAlarmManager.canScheduleExactAlarms())
                .thenReturn(true);

        // Act
        boolean result = MainActivity.checkAllNotificationPermissions(mockContext, android12);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testForAndroid10_NoChecks() {
        // Arrange
        Context mockContext = mock(Context.class);
        int android10 = 29; // Q

        // НЕ мокаем ничего, так как для Android 10 ничего не проверяется

        // Act
        boolean result = MainActivity.checkAllNotificationPermissions(mockContext, android10);

        // Assert
        assertTrue(result);
    }
}