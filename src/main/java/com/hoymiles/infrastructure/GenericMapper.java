package com.hoymiles.infrastructure;

public interface GenericMapper <S, D> {
    D map(S source);
}
