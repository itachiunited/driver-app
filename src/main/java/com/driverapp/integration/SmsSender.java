package com.driverapp.integration;
import com.messagebird.MessageBirdClient;
import com.messagebird.MessageBirdService;
import com.messagebird.MessageBirdServiceImpl;
import com.messagebird.exceptions.GeneralException;
import com.messagebird.exceptions.UnauthorizedException;
import com.messagebird.objects.MessageResponse;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SmsSender {
    public MessageResponse sendSMS(String toPhoneNumber) throws UnauthorizedException, GeneralException {
        // Create a MessageBirdService
        final MessageBirdService messageBirdService = new MessageBirdServiceImpl("yiTolu5C1Ynox530S545jk29a");
        // Add the service to the client
        final MessageBirdClient messageBirdClient = new MessageBirdClient(messageBirdService);
        // convert String number into acceptable format
        BigInteger phoneNumber = new BigInteger(toPhoneNumber);
        final List<BigInteger> phones = new ArrayList<BigInteger>();
        phones.add(phoneNumber);

        final MessageResponse response =
            messageBirdClient.sendMessage("+14016369040", "Hello World, I am a text message and I was hatched by Java code!", phones);
            System.out.println(response);
            return response;
    }
}
