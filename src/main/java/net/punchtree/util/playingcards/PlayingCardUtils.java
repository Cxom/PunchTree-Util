package net.punchtree.util.playingcards;

import net.punchtree.util.PunchTreeUtilPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class PlayingCardUtils {

    private static final NamespacedKey PLAYING_CARDS_KEY = new NamespacedKey(PunchTreeUtilPlugin.getInstance(), "playing_cards");

    private static final int PLAYING_CARD_MIN_CUSTOM_MODEL_DATA = 1001;
    private static final int PLAYING_CARD_MAX_CUSTOM_MODEL_DATA = 1052;

    private static final int PLAYING_CARD_STACK_MIN_CUSTOM_MODEL_DATA = 1001;
    private static final int PLAYING_CARD_STACK_MAX_CUSTOM_MODEL_DATA = 1052;

    static boolean isCardOrCardStack(ItemStack item) {
        return isFaceUpCard(item) || isFaceUpCardStack(item) || isFaceDownCard(item) || isFaceDownCardStack(item);
    }

    static boolean isSingleCard(ItemStack cardOrCardStackToAdd) {
        return isFaceUpCard(cardOrCardStackToAdd) || isFaceDownCard(cardOrCardStackToAdd);
    }

    static boolean isCardStack(ItemStack cardOrCardStackToAdd) {
        return isFaceUpCardStack(cardOrCardStackToAdd) || isFaceDownCardStack(cardOrCardStackToAdd);
    }

    static boolean isFaceUpCardStack(ItemStack item) {
        return item != null
                && item.getType() == PlayingCard.PLAYING_CARD_STACK_MATERIAL
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() >= PLAYING_CARD_STACK_MIN_CUSTOM_MODEL_DATA
                && item.getItemMeta().getCustomModelData() <= PLAYING_CARD_STACK_MAX_CUSTOM_MODEL_DATA;
    }

    static boolean isFaceUpCard(ItemStack item) {
        return item != null
                && item.getType() == PlayingCard.PLAYING_CARD_MATERIAL
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() >= PLAYING_CARD_MIN_CUSTOM_MODEL_DATA
                && item.getItemMeta().getCustomModelData() <= PLAYING_CARD_MAX_CUSTOM_MODEL_DATA;
    }

    static boolean isFaceDownCard(ItemStack item) {
        return item != null &&
                item.getType() == PlayingCard.PLAYING_CARD_MATERIAL
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() == 1000
                && item.getItemMeta().getPersistentDataContainer().has(PLAYING_CARDS_KEY);
    }

    static boolean isFaceDownCardStack(ItemStack item) {
        return item != null &&
                item.getType() == PlayingCard.PLAYING_CARD_STACK_MATERIAL
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() == 1000;
    }

    static void updateTopCardOfStack(BundleMeta bundleMeta) {
        bundleMeta.setCustomModelData(bundleMeta.getItems().get(0).getItemMeta().getCustomModelData());
    }

    static boolean isLastCardInStack(BundleMeta bundleMeta) {
        return bundleMeta.getItems().size() == 1;
    }

    static ItemStack flipCardOrCardStack(ItemStack item) {
        if (isFaceDownCard(item)) {
            return flipFaceDownCard(item);
        } else if (isFaceDownCardStack(item)) {
            item.editMeta(meta -> {
                BundleMeta bundleMeta = (BundleMeta) meta;
//                ItemStack topCard = flipFaceDownCard(bundleMeta.getItems().get(0));
                ItemStack topCard = bundleMeta.getItems().get(0);
                meta.setCustomModelData(topCard.getItemMeta().getCustomModelData());
                meta.displayName(PlayingCard.fromItem(topCard).getName());
//                bundleMeta.setItems(bundleMeta.getItems().stream().map(PlayingCardUtils::flipFaceDownCard).collect(Collectors.toList()));
            });
            return item;
        } else if (isFaceUpCard(item)) {
            return flipFaceUpCard(item);
        } else if (isFaceUpCardStack(item)) {
            item.editMeta(meta -> {
                meta.setCustomModelData(1000);
                BundleMeta bundleMeta = (BundleMeta) meta;
//                bundleMeta.setItems(bundleMeta.getItems().stream().map(PlayingCardUtils::flipFaceUpCard).collect(Collectors.toList()));
                bundleMeta.displayName(PlayingCard.FACE_DOWN_CARD_PILE_NAME);
            });

//            ItemStack faceDownPile = PlayingCard.getNewFaceDownPileItem();
//            List<PlayingCard> cards = ((BundleMeta) item.getItemMeta()).getItems().stream().map(PlayingCard::fromItem).collect(Collectors.toList());
//            writeCardsToPdc(faceDownPile, cards);
            return item;
        } else {
            return item;
        }
    }

    @Nullable
    private static ItemStack flipFaceDownCard(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<PlayingCard> cards = meta.getPersistentDataContainer().get(PLAYING_CARDS_KEY, new CardStackPersistentDataType());
        return PlayingCard.getItemForCardList(cards);
    }

    @NotNull
    private static ItemStack flipFaceUpCard(ItemStack item) {
        ItemStack faceDownCard = PlayingCard.getNewFaceDownCardItem();
        writeCardToPdc(faceDownCard, PlayingCard.fromItem(item));
        return faceDownCard;
    }

    static void writeCardToPdc(ItemStack faceDownCard, PlayingCard fromItem) {
        writeCardsToPdc(faceDownCard, List.of(fromItem));
    }

    static void writeCardsToPdc(ItemStack faceDownCard, List<PlayingCard> cards) {
        faceDownCard.editMeta(meta -> {
            meta.getPersistentDataContainer().set(PLAYING_CARDS_KEY, new CardStackPersistentDataType(), cards);
        });
    }

    static ItemStack combineCardStacks(ItemStack cardsOnTop, ItemStack cardsOnBottom) {

        // We combine in the order top then bottom, but use the face-up status of the bottom

        ItemStack combinedStack;
        if (isFaceUpCard(cardsOnBottom)) {
            combinedStack = PlayingCard.fromItem(cardsOnBottom).getNewPileItem();
        } else if (isFaceDownCard(cardsOnBottom)) {
            combinedStack = flipCardOrCardStack(PlayingCard.fromItem(flipCardOrCardStack(cardsOnBottom)).getNewPileItem());
        } else /* isCardStack */ {
            combinedStack = cardsOnBottom;
        }

        BundleMeta combinedStackMeta = (BundleMeta) combinedStack.getItemMeta();
        Stream<ItemStack> bottomCardsStream = combinedStackMeta.getItems().stream();
        Stream<ItemStack> topCardsStream =
                isFaceUpCard(cardsOnTop) ? Stream.of(cardsOnTop) :
                isFaceDownCard(cardsOnTop) ? Stream.of(flipCardOrCardStack(cardsOnTop)) :
                /* isCardStack */ ((BundleMeta) cardsOnTop.getItemMeta()).getItems().stream();

        List<ItemStack> items = Stream.concat(topCardsStream, bottomCardsStream).toList();
        combinedStackMeta.setItems(items);

        if (!isFaceDownCardStack(combinedStack)) {
            updateTopCardOfStack(combinedStackMeta);
        }

        combinedStack.setItemMeta(combinedStackMeta);
        return combinedStack;
    }
}
