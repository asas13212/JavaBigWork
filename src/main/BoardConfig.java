package main;

import architecture.*;
import architecture.Event;

import java.awt.*;

public class BoardConfig
{
    public record PlayerInfoLayout(int imgX, int imgY, int imgW, int imgH,
                                    int moneyX, int moneyY,
                                    int propertyX, int propertyY,
                                    int hpX, int hpY) {}

    public static final PlayerInfoLayout P1_LAYOUT = new PlayerInfoLayout(
        10, 60, 340, 110,
        200, 102,
        240, 145,
        210, 123
    );

    public static final PlayerInfoLayout P2_LAYOUT = new PlayerInfoLayout(
        10, 200, 340, 110,
        200, 242,
        240, 288,
        210, 263
    );

    public static final Rectangle PROP_CARD_BG = new Rectangle(900, 950, 500, 87);

    private final Point[] points;
    private final Tile[] tiles;

    public BoardConfig()
    {
        points = new Point[]{
                new Point(235, 515), new Point(305, 465), new Point(375, 415), new Point(445, 360), new Point(510, 310),
                new Point(580, 265), new Point(650, 215), new Point(720, 165), new Point(790, 135), new Point(860, 185),
                new Point(925, 230), new Point(990, 275), new Point(1050, 320), new Point(1120, 370), new Point(1190, 415),
                new Point(1260, 465), new Point(1190, 515), new Point(1120, 565), new Point(1050, 615), new Point(990, 665),
                new Point(925, 715), new Point(860, 765), new Point(790, 815), new Point(720, 865), new Point(650, 815),
                new Point(580, 765), new Point(510, 715), new Point(445, 665), new Point(375, 615), new Point(305, 565)
        };

        tiles = new Tile[]{
                new Start(new Point()), new Event(1, new Point(), "小吃街"),
                new ResidentLand(2, new Point(280, 375), "居民楼1"),
                new ResidentLand(3, new Point(350, 325), "居民楼2"),
                new GymLand(4, new Point(420, 275), "体育馆1"),
                new HotelLand(5, new Point(415, 165), "旅馆"),
                new Empty(6, new Point(), "空地"),
                new Chance(7, new Point(), "抽卡点"),
                new Shop(8, new Point(), "商店"),

                new Hospital(9, new Point(), "医院"),
                new ResidentLand(10, new Point(960, 185), "居民楼3"),
                new ParkLand(11, new Point(1026, 210), "公园"),
                new Empty(12, new Point(), "空地"),
                new ResidentLand(13, new Point(1160, 330), "居民楼4"),
                new Event(14, new Point(), "市民公园"),
                new Prison(15, new Point(), "牢中"),

                new Chance(16, new Point(), "抽卡点2"),
                new GymLand(17, new Point(1028, 518), "体育馆2"),
                new ResidentLand(18, new Point(958, 568), "居民楼5"),
                new ResidentLand(19, new Point(958 - 70, 618), "居民楼6"),
                new ResidentLand(20, new Point(958 - 140, 668), "居民楼7"),
                new Hospital(21, new Point(), "医院2"),
                new Chance(22, new Point(), "抽卡点3"),
                new Event(23, new Point(), "上饶中学"),
                new Empty(24, new Point(), "空地"),

                new ResidentLand(25, new Point(620, 715), "居民楼8"),
                new ShopLand(26, new Point(483, 600), "超市"),
                new Empty(27, new Point(), "空地"),
                new ResidentLand(28, new Point(419, 570), "居民楼9"),
                new Empty(29, new Point(), "空地")
        };
    }

    public Point[] getPoints() { return points; }
    public Tile[] getTiles() { return tiles; }
}
