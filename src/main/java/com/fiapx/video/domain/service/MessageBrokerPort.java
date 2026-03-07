package com.fiapx.video.domain.service;

import com.fiapx.video.domain.entity.Video;

public interface MessageBrokerPort {
    void sendToProcessQueue(Video video);
}
