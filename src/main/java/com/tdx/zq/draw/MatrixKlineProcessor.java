package com.tdx.zq.draw;

import java.util.ArrayList;
import java.util.List;

import com.tdx.zq.draw.PeakKlineProcessor.MatrixKlineRow;
import com.tdx.zq.enums.PeakShapeEnum;

public class MatrixKlineProcessor {

    private List<MatrixKlineRow> matrixKlineRowList;
    private List<MatrixKlineRow> upMatrixKlineRowList;
    private List<MatrixKlineRow> downMatrixKlineRowList;
    private List<List<MatrixKlineRow>> upMatrixList;
    private List<List<MatrixKlineRow>> downMatrixList;


    public MatrixKlineProcessor(List<MatrixKlineRow> matrixKlineRowList) {
        this.matrixKlineRowList = matrixKlineRowList;
        this.upMatrixList = new ArrayList<>();
        this.downMatrixList = new ArrayList<>();
        setMatrixTendency();
    }

    public void setMatrixTendency() {
        for (int i = 0; i < matrixKlineRowList.size() - 6; i++) {
            List<MatrixKlineRow> matrixKlineRows = new ArrayList<>();
            MatrixKlineRow r1 = matrixKlineRowList.get(i);
            MatrixKlineRow r2 = matrixKlineRowList.get(i + 1);
            MatrixKlineRow r3 = matrixKlineRowList.get(i + 2);
            MatrixKlineRow r4 = matrixKlineRowList.get(i + 3);
            MatrixKlineRow r5 = matrixKlineRowList.get(i + 4);
            MatrixKlineRow r6 = matrixKlineRowList.get(i + 5);
            if (matrixKlineRowList.get(i).getShape() == PeakShapeEnum.FLOOR) {
                if (upMatrixList.size() == 0) {
                    if (r1.getLow() <= r3.getLow()) {
                        matrixKlineRows.add(r1);
                        matrixKlineRows.add(r2);
                        matrixKlineRows.add(r3);
                        matrixKlineRows.add(r4);
                        upMatrixList.add(matrixKlineRows);
                    }
                } else {
                    List<MatrixKlineRow> lastUpMatrixList = upMatrixList.get(upMatrixList.size() - 1);
                    if (lastUpMatrixList.get(lastUpMatrixList.size() - 1) == r2) {
                        int max = - 1;
                        boolean up = false;
                        for (int j = lastUpMatrixList.size() - 1; j > 1; j -= 2) {
                            if (!up && lastUpMatrixList.get(j).getHigh() >= lastUpMatrixList.get(1).getHigh()) {
                                up = true;
                            }
                            if (max == -1) {
                                max = j;
                            } else {
                                max = lastUpMatrixList.get(max).getHigh() > lastUpMatrixList.get(j).getHigh() ? max : j;
                            }
                        }
                        if (up) {
                            if (max == lastUpMatrixList.size() - 1) {
                                if (r5.getLow() < r3.getLow() && r6.getHigh() <= r2.getHigh()
                                        && r4.getHigh() <= lastUpMatrixList.get(max).getHigh()) {
                                    for (int z = lastUpMatrixList.size() - 1; z > max; z--) {
                                        lastUpMatrixList.remove(z);
                                   }
                                } else {
                                    lastUpMatrixList.add(r3);
                                    lastUpMatrixList.add(r4);
                                }
                            } else {
                                if (r5.getLow() < lastUpMatrixList.get(max + 1).getLow()
                                        && r6.getHigh() <= lastUpMatrixList.get(max).getHigh()) {
                                    for (int z = lastUpMatrixList.size() - 1; z > max; z--) {
                                        lastUpMatrixList.remove(z);
                                    }
                                } else {
                                    lastUpMatrixList.add(r3);
                                    lastUpMatrixList.add(r4);
                                }
                            }
                        } else {
                            if (r3.getLow() >= lastUpMatrixList.get(0).getLow()) {
                                lastUpMatrixList.add(r3);
                                lastUpMatrixList.add(r4);
                            } else {
                                upMatrixList.remove(upMatrixList.size() - 1);
                            }
                        }
                    } else {
                        if (r1.getLow() <= r3.getLow()) {
                            matrixKlineRows.add(r1);
                            matrixKlineRows.add(r2);
                            matrixKlineRows.add(r3);
                            matrixKlineRows.add(r4);
                            upMatrixList.add(matrixKlineRows);
                        }
                    }
                }
            }
            else {
                if (downMatrixList.size() == 0) {
                    if (r1.getHigh() >= r3.getHigh()) {
                        matrixKlineRows.add(r1);
                        matrixKlineRows.add(r2);
                        matrixKlineRows.add(r3);
                        matrixKlineRows.add(r4);
                        downMatrixList.add(matrixKlineRows);
                    }
                } else {
                    List<MatrixKlineRow> lastDownMatrixList = downMatrixList.get(downMatrixList.size() - 1);
                    if (lastDownMatrixList.get(lastDownMatrixList.size() - 1) == r2) {
                        int min = - 1;
                        boolean down = false;
                        for (int j = lastDownMatrixList.size() - 1; j > 1; j -= 2) {
                            if (!down && lastDownMatrixList.get(j).getLow() <= lastDownMatrixList.get(1).getLow()) {
                                down = true;
                            }
                            if (min == -1) {
                                min = j;
                            } else {
                                min = lastDownMatrixList.get(min).getLow() < lastDownMatrixList.get(j).getLow() ? min : j;
                            }
                        }
                        if (down) {
                            if (min == lastDownMatrixList.size() - 1) {
                                if (r5.getHigh() > r3.getHigh() && r6.getLow() >= r2.getLow()
                                        && r4.getLow() >= lastDownMatrixList.get(min).getLow()) {
                                    for (int z = lastDownMatrixList.size() - 1; z > min; z--) {
                                        lastDownMatrixList.remove(z);
                                    }
                                } else {
                                    lastDownMatrixList.add(r3);
                                    lastDownMatrixList.add(r4);
                                }
                            } else {
                                if (r5.getHigh() > lastDownMatrixList.get(min + 1).getHigh()
                                        && r6.getLow() >= lastDownMatrixList.get(min).getLow()) {
                                    for (int z = lastDownMatrixList.size() - 1; z > min; z--) {
                                        lastDownMatrixList.remove(z);
                                    }
                                } else {
                                    lastDownMatrixList.add(r3);
                                    lastDownMatrixList.add(r4);
                                }
                            }
                        } else {
                            if (r3.getHigh() <= lastDownMatrixList.get(0).getHigh()) {
                                lastDownMatrixList.add(r3);
                                lastDownMatrixList.add(r4);
                            } else {
                                downMatrixList.remove(downMatrixList.size() - 1);
                            }
                        }
                    } else {
                        if (r1.getHigh() >= r3.getHigh()) {
                            matrixKlineRows.add(r1);
                            matrixKlineRows.add(r2);
                            matrixKlineRows.add(r3);
                            matrixKlineRows.add(r4);
                            downMatrixList.add(matrixKlineRows);
                        }
                    }
                }
            }
        }
        upMatrixList.stream().forEach(m -> System.out.println("up:" + m.get(0).getDate() + "_" + m.get(m.size() - 1).getDate()));
        System.out.println("---------------------------------------");
        System.out.println("---------------------------------------");
        System.out.println("---------------------------------------");
        downMatrixList.stream().forEach(m -> System.out.println("down:" + m.get(0).getDate() + "_" + m.get(m.size() - 1).getDate()));
        System.out.println("---------------------------------------");
        System.out.println("---------------------------------------");
        System.out.println("---------------------------------------");

        long targetDate = upMatrixList.get(0).get(0).getDate() < downMatrixList.get(0).get(0).getDate()
                ? upMatrixList.get(0).get(0).getDate() : downMatrixList.get(0).get(0).getDate();
        while (true) {
            List<MatrixKlineRow> currKlineRowList = null;
            for (int i = 0; i < upMatrixList.size(); i++) {
                if (targetDate == upMatrixList.get(i).get(0).getDate()) {
                    currKlineRowList = upMatrixList.get(i);
                    targetDate = currKlineRowList.get(currKlineRowList.size() - 1).getDate();
                    System.out.println("up:     " + currKlineRowList.get(0).getDate() + "_" + currKlineRowList.get(currKlineRowList.size() - 1).getDate());
                    break;
                }
            }
            for (int i = 0; i < downMatrixList.size(); i++) {
                if (targetDate == downMatrixList.get(i).get(0).getDate()) {
                    currKlineRowList = downMatrixList.get(i);
                    targetDate = currKlineRowList.get(currKlineRowList.size() - 1).getDate();
                    System.out.println("down:   " + currKlineRowList.get(0).getDate() + "_" + currKlineRowList.get(currKlineRowList.size() - 1).getDate());
                    break;
                }
            }
            if (currKlineRowList == null) {
                break;
            }
        }
    }

//    public void setMatrixTendency() {
//        for (int i = 0; i < matrixKlineRowList.size() - 4; i++) {
//            List<MatrixKlineRow> matrixKlineRows = new ArrayList<>();
//            MatrixKlineRow r1 = matrixKlineRowList.get(i);
//            MatrixKlineRow r2 = matrixKlineRowList.get(i + 1);
//            MatrixKlineRow r3 = matrixKlineRowList.get(i + 2);
//            MatrixKlineRow r4 = matrixKlineRowList.get(i + 3);
//            if (matrixKlineRowList.get(i).getShape() == PeakShapeEnum.FLOOR) {
//                if (upMatrixList.size() == 0) {
//                    if (r1.getLow() <= r3.getLow()) {
//                        matrixKlineRows.add(r1);
//                        matrixKlineRows.add(r2);
//                        matrixKlineRows.add(r3);
//                        matrixKlineRows.add(r4);
//                        upMatrixList.add(matrixKlineRows);
//                    }
//                } else {
//                    List<MatrixKlineRow> lastUpMatrixList = upMatrixList.get(upMatrixList.size() - 1);
//                    if (lastUpMatrixList.get(lastUpMatrixList.size() - 1) == r2) {
//                        boolean up = false;
//                        for (int j = lastUpMatrixList.size() - 1; j > 1; j -= 2) {
//                            if (lastUpMatrixList.get(j).getHigh() >= lastUpMatrixList.get(1).getHigh()) {
//                                up = true;
//                                break;
//                            }
//                        }
//                        if (up) {
//                            if (r3.getLow() > lastUpMatrixList.get(lastUpMatrixList.size() - 3).getHigh()) {
//                                lastUpMatrixList.add(r3);
//                                lastUpMatrixList.add(r4);
//                            } else {
//                                if (r4.getHigh() >= lastUpMatrixList.get(lastUpMatrixList.size() - 1).getHigh()) {
//                                    lastUpMatrixList.add(r3);
//                                    lastUpMatrixList.add(r4);
//                                } else {
//                                    upMatrixList.remove(upMatrixList.size() - 1);
//                                }
//                            }
//                        } else {
//                            if (r3.getLow() >= lastUpMatrixList.get(0).getLow()) {
//                                lastUpMatrixList.add(r3);
//                                lastUpMatrixList.add(r4);
//                            } else {
//                                upMatrixList.remove(upMatrixList.size() - 1);
//                            }
//                        }
//                    } else {
//                        if (r1.getLow() <= r3.getLow()) {
//                            matrixKlineRows.add(r1);
//                            matrixKlineRows.add(r2);
//                            matrixKlineRows.add(r3);
//                            matrixKlineRows.add(r4);
//                            upMatrixList.add(matrixKlineRows);
//                        }
//                    }
//                }
//            } else {
//                if (downMatrixList.size() == 0) {
//                    if (r1.getHigh() >= r3.getHigh()) {
//                        matrixKlineRows.add(r1);
//                        matrixKlineRows.add(r2);
//                        matrixKlineRows.add(r3);
//                        matrixKlineRows.add(r4);
//                        downMatrixList.add(matrixKlineRows);
//                    }
//                } else {
//                    List<MatrixKlineRow> lastUpMatrixList = downMatrixList.get(downMatrixList.size() - 1);
//                    if (lastUpMatrixList.get(lastUpMatrixList.size() - 1) == r2) {
//                        boolean down = false;
//                        for (int j = lastUpMatrixList.size() - 1; j > 1; j -= 2) {
//                            if (lastUpMatrixList.get(j).getLow() <= lastUpMatrixList.get(1).getLow()) {
//                                down = true;
//                                break;
//                            }
//                        }
//                        if (down) {
//                            if (r3.getHigh() > lastUpMatrixList.get(lastUpMatrixList.size() - 3).getLow()) {
//                                lastUpMatrixList.add(r3);
//                                lastUpMatrixList.add(r4);
//                            }
//                        } else {
//                            if (r3.getHigh() <= lastUpMatrixList.get(0).getHigh()) {
//                                lastUpMatrixList.add(r3);
//                                lastUpMatrixList.add(r4);
//                            } else {
//                                downMatrixList.remove(downMatrixList.size() - 1);
//                            }
//                        }
//                    } else {
//                        if (r1.getHigh() >= r3.getHigh()) {
//                            matrixKlineRows.add(r1);
//                            matrixKlineRows.add(r2);
//                            matrixKlineRows.add(r3);
//                            matrixKlineRows.add(r4);
//                            downMatrixList.add(matrixKlineRows);
//                        }
//                    }
//                }
//            }
//        }
//        upMatrixList.stream().forEach(m -> System.out.println("up:" + m.get(0).getDate() + "_" + m.get(m.size() - 1).getDate()));
//        System.out.println("---------------------------------------");
//        System.out.println("---------------------------------------");
//        System.out.println("---------------------------------------");
//        downMatrixList.stream().forEach(m -> System.out.println("down:" + m.get(0).getDate() + "_" + m.get(m.size() - 1).getDate()));
//    }


//    public void setMatrixTendency() {
//        for (int i = 0; i < matrixKlineRowList.size() - 4; i++) {
//            List<MatrixKlineRow> matrixKlineRows = new ArrayList<>();
//            MatrixKlineRow r1 = matrixKlineRowList.get(i);
//            MatrixKlineRow r2 = matrixKlineRowList.get(i + 1);
//            MatrixKlineRow r3 = matrixKlineRowList.get(i + 2);
//            MatrixKlineRow r4 = matrixKlineRowList.get(i + 3);
//            if (matrixKlineRowList.get(i).getShape() == PeakShapeEnum.FLOOR) {
//                if (r1.getLow() <= r3.getLow()) {
//                    if (upMatrixList.size() != 0) {
//                        List<MatrixKlineRow> lastUpMatrixList = upMatrixList.get(upMatrixList.size() - 1);
//                        if (lastUpMatrixList.get(lastUpMatrixList.size() - 1) == r2) {
//                            if (lastUpMatrixList.size() >= 5) {
//                                boolean up = false;
//                                for (int j = lastUpMatrixList.size() - 1; j > 1; j -= 2) {
//                                    if (lastUpMatrixList.get(j).getHigh() > lastUpMatrixList.get(1).getHigh()) {
//                                        up = true;
//                                        break;
//                                    }
//                                }
//                                 if (up && r1.getLow() <= lastUpMatrixList.get(lastUpMatrixList.size() - 3).getLow()) {
//                                     continue;
//                                }
//                            }
//                            lastUpMatrixList.add(r3);
//                            lastUpMatrixList.add(r4);
//                            continue;
//                        }
//                    }
//                    matrixKlineRows.add(r1);
//                    matrixKlineRows.add(r2);
//                    matrixKlineRows.add(r3);
//                    matrixKlineRows.add(r4);
//                    upMatrixList.add(matrixKlineRows);
//                }
//            } else {
//                if (r1.getHigh() >= r3.getHigh()) {
//                    if (downMatrixList.size() != 0) {
//                        List<MatrixKlineRow>  lastDownMatrixList = downMatrixList.get(downMatrixList.size() - 1);
//                        if (lastDownMatrixList.get(lastDownMatrixList.size() - 1) == r2) {
//                            if (lastDownMatrixList.size() >= 5) {
//                                boolean down = false;
//                                for (int j = lastDownMatrixList.size() - 1; j > 1; j -= 2) {
//                                    if (lastDownMatrixList.get(j).getLow() < lastDownMatrixList.get(1).getLow()) {
//                                        down = true;
//                                        break;
//                                    }
//                                }
//                                if (down && r1.getHigh() >= lastDownMatrixList.get(lastDownMatrixList.size() - 3).getHigh()) {
//                                    continue;
//                                }
//                            }
//                            lastDownMatrixList.add(r3);
//                            lastDownMatrixList.add(r4);
//                            continue;
//                        }
//                    }
//                    matrixKlineRows.add(r1);
//                    matrixKlineRows.add(r2);
//                    matrixKlineRows.add(r3);
//                    matrixKlineRows.add(r4);
//                    downMatrixList.add(matrixKlineRows);
//                }
//            }
//        }
//        upMatrixList.stream().forEach(m -> System.out.println("up:" + m.get(0).getDate() + "_" + m.get(m.size() - 1).getDate()));
//        System.out.println("-----------------------------------");
//        System.out.println("-----------------------------------");
//        System.out.println("-----------------------------------");
//        downMatrixList.stream().forEach(m -> System.out.println("down:" + m.get(0).getDate() + "_" + m.get(m.size() - 1).getDate()));
//    }

}
