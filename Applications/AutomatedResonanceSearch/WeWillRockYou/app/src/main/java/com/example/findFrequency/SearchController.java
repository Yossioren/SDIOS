package com.example.findFrequency;

interface SearchController {
    void updateSearch(SensorsE toUpdate);

    enum SensorsE {Gyroscope, Accelerometer, Magenetometer}
}
