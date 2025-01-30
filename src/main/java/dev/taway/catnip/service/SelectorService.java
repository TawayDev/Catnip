package dev.taway.catnip.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SelectorService {

    /**
     * Selects one string out of the inputted array.
     *
     * @param args Array to be selected from.
     * @return Random selection.
     */
    public String selectOne(String[] args) {
        if (args.length == 0) return null;
        if (args.length == 1) return args[0];

        Random random = new Random();
        int randomIndex = random.nextInt(args.length);
        return args[randomIndex];
    }
}
