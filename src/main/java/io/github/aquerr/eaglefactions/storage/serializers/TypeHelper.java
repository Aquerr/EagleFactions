/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.aquerr.eaglefactions.storage.serializers;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.Tuple;

import java.util.Arrays;
import java.util.List;

/**
 * This class has been copied from Nuclues.
 *
 * <p>See https://github.com/NucleusPowered/Nucleus/blob/sponge-api/7/src/main/java/io/github/nucleuspowered/nucleus/util/TypeHelper.java</p>
 *
 * <p>This class, as such, is copyrighted (c) by NucleusPowered team and Nucleus contributors.</p>
 */
public final class TypeHelper {

    private TypeHelper() {}

    public static Tuple<DataQuery, List<?>> getList(DataQuery query, Object array) {

        if (array instanceof byte[]) {
            return Tuple.of(getNewName(query, "B"), Arrays.asList(ArrayUtils.toObject((byte[]) array)));
        } else if (array instanceof short[]) {
            return Tuple.of(getNewName(query, "S"), Arrays.asList(ArrayUtils.toObject((short[]) array)));
        } else if (array instanceof int[]) {
            return Tuple.of(getNewName(query, "I"), Arrays.asList(ArrayUtils.toObject((int[]) array)));
        } else if (array instanceof long[]) {
            return Tuple.of(getNewName(query, "J"), Arrays.asList(ArrayUtils.toObject((long[]) array)));
        } else if (array instanceof float[]) {
            return Tuple.of(getNewName(query, "F"), Arrays.asList(ArrayUtils.toObject((float[]) array)));
        } else if (array instanceof double[]) {
            return Tuple.of(getNewName(query, "D"), Arrays.asList(ArrayUtils.toObject((double[]) array)));
        } else if (array instanceof boolean[]) {
            return Tuple.of(getNewName(query, "Z"), Arrays.asList(ArrayUtils.toObject((boolean[]) array)));
        }

        throw new RuntimeException();
    }

    public static Tuple<DataQuery, Object> getArray(DataQuery query, DataView container) {
        String a = query.asString(".");
        DataQuery q = DataQuery.of('.', query.asString(".").replaceAll("\\$Array\\$[a-zA-Z]$", ""));
        String objType = a.substring(a.length() - 1);
        List<?> array = container.getList(query).orElse(Lists.newArrayList());
        int size = array.size();

        switch (objType) {
            case "B": {
                byte[] b = new byte[size];
                for (int i = 0; i < size; i++) {
                    Object obj = array.get(i);
                    if (obj instanceof String) {
                        b[i] = Byte.parseByte((String) obj);
                    } else {
                        b[i] = ((Number) obj).byteValue();
                    }
                }

                return Tuple.of(q, b);
            }
            case "S": {
                short[] b = new short[size];
                for (int i = 0; i < size; i++) {
                    Object obj = array.get(i);
                    if (obj instanceof String) {
                        b[i] = Short.parseShort((String) obj);
                    } else {
                        b[i] = ((Number) obj).shortValue();
                    }
                }

                return Tuple.of(q, b);
            }
            case "I": {
                int[] b = new int[size];
                for (int i = 0; i < size; i++) {
                    Object obj = array.get(i);
                    if (obj instanceof String) {
                        b[i] = Integer.parseInt((String) obj);
                    } else {
                        b[i] = ((Number) obj).intValue();
                    }
                }

                return Tuple.of(q, b);
            }
            case "J": {
                long[] b = new long[size];
                for (int i = 0; i < size; i++) {
                    Object obj = array.get(i);
                    if (obj instanceof String) {
                        b[i] = Long.parseLong((String) obj);
                    } else {
                        b[i] = ((Number) obj).longValue();
                    }
                }

                return Tuple.of(q, b);
            }
            case "F": {
                float[] b = new float[size];
                for (int i = 0; i < size; i++) {
                    Object obj = array.get(i);
                    if (obj instanceof String) {
                        b[i] = Float.parseFloat((String) obj);
                    } else {
                        b[i] = ((Number) obj).floatValue();
                    }
                }

                return Tuple.of(q, b);
            }
            case "D": {
                double[] b = new double[size];
                for (int i = 0; i < size; i++) {
                    Object obj = array.get(i);
                    if (obj instanceof String) {
                        b[i] = Double.parseDouble((String) obj);
                    } else {
                        b[i] = ((Number) obj).doubleValue();
                    }
                }

                return Tuple.of(q, b);
            }
            case "Z": {
                boolean[] b = new boolean[size];
                for (int i = 0; i < size; i++) {
                    Object obj = array.get(i);
                    if (obj instanceof String) {
                        b[i] = Boolean.parseBoolean((String) obj);
                    } else {
                        b[i] = (Boolean) obj;
                    }
                }

                return Tuple.of(q, b);
            }
        }

        throw new RuntimeException();
    }

    private static DataQuery getNewName(DataQuery dataQuery, String name) {
        return DataQuery.of('.', dataQuery.asString(".") + "$Array$" + name);
    }
}