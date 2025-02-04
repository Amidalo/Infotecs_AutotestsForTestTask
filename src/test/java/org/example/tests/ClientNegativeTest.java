package org.example.tests;

import org.example.Client;
import org.example.JSONProcessing;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class ClientNegativeTest {
    private Client client;
    private JSONProcessing jsonProcessing;

    @BeforeClass
    public void setUp() throws JSchException, IOException, SftpException {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream("config.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IOException("Не удалось загрузить файл конфигурации: config.properties", e);
        }

        String address = properties.getProperty("address");
        String port = properties.getProperty("port");
        String login = properties.getProperty("login");
        String password = properties.getProperty("password");
        String filepath = properties.getProperty("filepath");

        client = new Client();
        client.connect(address, Integer.parseInt(port), login, password);
        jsonProcessing = new JSONProcessing(client, filepath);
    }

    @AfterClass
    public void tearDown() {
        client.disconnect();
    }

    @Test(priority = 1, expectedExceptions = JSchException.class)
    public void testFailedConnection() throws JSchException {
        Client failedClient = new Client();
        failedClient.connect("server", 22, "user", "password");
    }

    @Test(priority = 2)
    public void testGetIPByNonExistentDomain() {
        Assert.assertEquals(jsonProcessing.getIPByDomain("nonexistent.ru"), "Такого домена в файле нет.",
                "Несуществующий домен не должен возвращать IP-адрес.");
    }

    @Test(priority = 3)
    public void testGetDomainByNonExistentIP() {
        Assert.assertEquals(jsonProcessing.getDomainByIP("0.0.0.0"), "Такого IP-адреса в файле нет.",
                "Несуществующий IP-адрес не должен возвращать домен.");
    }

    @Test(priority = 4)
    public void testAddExistingDomain() throws IOException, SftpException {
        jsonProcessing.addNewPairOfDomainAddress("test11.ru", "192.168.1.11");
        jsonProcessing.addNewPairOfDomainAddress("test11.ru", "192.168.1.12");
        Assert.assertEquals(jsonProcessing.getIPByDomain("test11.ru"), "192.168.1.11",
                "Домен не должен быть перезаписан.");
    }

    @Test(priority = 5)
    public void testAddExistingIP() throws IOException, SftpException {
        jsonProcessing.addNewPairOfDomainAddress("test12.ru", "192.168.1.12");
        jsonProcessing.addNewPairOfDomainAddress("test121.com", "192.168.1.12");
        Assert.assertEquals(jsonProcessing.getDomainByIP("192.168.1.12"), "test12.ru",
                "IP-адрес не должен быть перезаписан.");
    }

    @Test(priority = 6)
    public void testAddInvalidIP() throws IOException, SftpException {
        jsonProcessing.addNewPairOfDomainAddress("test13.ru", "invalid.ip");
        Assert.assertEquals(jsonProcessing.getIPByDomain("test13.ru"), "Такого домена в файле нет.",
                "Некорректный IP-адрес не должен быть добавлен.");
    }

    @Test(priority = 7)
    public void testRemoveNonExistentDomain() throws IOException, SftpException {
        jsonProcessing.removePairOfDomainAddressByDomain("nonexistent.ru");
        Assert.assertEquals(jsonProcessing.getIPByDomain("nonexistent.ru"), "Такого домена в файле нет.",
                "Несуществующий домен не должен быть удален.");
    }

    @Test(priority = 8)
    public void testRemoveNonExistentIP() throws IOException, SftpException {
        jsonProcessing.removePairOfDomainAddressByIP("0.0.0.0");
        Assert.assertEquals(jsonProcessing.getDomainByIP("0.0.0.0"), "Такого IP-адреса в файле нет.",
                "Несуществующий IP-адрес не должен быть удален.");
    }
}
