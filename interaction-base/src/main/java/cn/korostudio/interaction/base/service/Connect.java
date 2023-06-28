package cn.korostudio.interaction.base.service;

public interface Connect {
    void sendMessage(byte[] data);
    void close();
}
