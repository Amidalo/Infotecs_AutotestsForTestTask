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

public class ClientPositiveTest {
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

    @Test(priority = 1)
    public void testSuccessfulConnection() {
        Assert.assertTrue(client.getSession().isConnected(), "Сессия должна быть подключена.");
        Assert.assertTrue(client.getSftpChannel().isConnected(), "SFTP канал должен быть подключен.");
    }

    @Test(priority = 2)
    public void testReadValidJSONFile() {
        jsonProcessing.getAddressesList();
    }

    @Test(priority = 3)
    public void testAddNewDomainIPPair() throws IOException, SftpException {
        jsonProcessing.addNewPairOfDomainAddress("test3.com", "192.168.1.3");
        jsonProcessing.getAddressesList();
    }

    @Test(priority = 4)
    public void testGetIPByDomain() throws SftpException, IOException {
        jsonProcessing.addNewPairOfDomainAddress("test4.ru", "192.168.1.4");
        Assert.assertEquals(jsonProcessing.getIPByDomain("test4.ru"), "192.168.1.4",
                "IP-адрес должен соответствовать домену.");
    }

    @Test(priority = 5)
    public void testGetDomainByIP() throws SftpException, IOException {
        jsonProcessing.addNewPairOfDomainAddress("test5.ru", "192.168.1.5");
        Assert.assertEquals(jsonProcessing.getDomainByIP("192.168.1.5"), "test5.ru",
                "Домен должен соответствовать IP-адресу.");
    }


    @Test(priority = 6)
    public void testRemoveDomainIPPairByDomain() throws IOException, SftpException {
        jsonProcessing.addNewPairOfDomainAddress("test6.ru", "192.168.1.6");
        jsonProcessing.removePairOfDomainAddressByDomain("test6.ru");
        Assert.assertEquals(jsonProcessing.getIPByDomain("test6.ru"), "Такого домена в файле нет.",
                "Домен должен быть удален.");
    }

    @Test(priority = 7)
    public void testRemoveDomainIPPairByIP() throws IOException, SftpException {
        jsonProcessing.addNewPairOfDomainAddress("test7.ru", "192.168.1.7");
        jsonProcessing.removePairOfDomainAddressByIP("192.168.1.7");
        Assert.assertEquals(jsonProcessing.getDomainByIP("192.168.1.7"), "Такого IP-адреса в файле нет.",
                "IP-адрес должен быть удален.");
    }
}
