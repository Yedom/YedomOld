package ru.mralexeimk.yedom.interfaces.validation;

import javax.validation.GroupSequence;

@GroupSequence({FirstOrder.class, SecondOrder.class, ThirdOrder.class, FourthOrder.class, FifthOrder.class, SixthOrder.class})
public interface OrderedChecks {
}