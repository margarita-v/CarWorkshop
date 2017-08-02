package com.dsr_practice.car_workshop.dialogs;

import com.dsr_practice.car_workshop.models.common.Task;

public interface CloseCallback {
    void onJobClose(boolean isTaskClosed, Task task);
}
