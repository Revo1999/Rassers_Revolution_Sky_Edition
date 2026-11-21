package dk.revo.revostorage;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

@GameTestHolder(RevoStorage.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StorageMultiplayerGameTests {
    private static net.minecraft.server.level.ServerPlayer viewer(GameTestHelper test, String name) {
        return FakePlayerFactory.get(test.getLevel(), new GameProfile(UUID.randomUUID(), name));
    }

    @GameTest(template = "storage_test")
    public static void springMovesLegacyChargeOnceAndHasNoMenu(GameTestHelper test) {
        var terminalPos = new BlockPos(1, 1, 1);
        var springPos = terminalPos.east();
        test.setBlock(terminalPos, RevoStorage.TERMINAL.get());
        var terminal = (StorageEntity) test.getBlockEntity(terminalPos);
        var registries = test.getLevel().registryAccess();
        var legacy = terminal.saveWithoutMetadata(registries);
        legacy.putInt("Charge", 2048);
        terminal.loadWithComponents(legacy, registries);
        test.assertTrue(terminal.springCharge() == -1, "An old terminal still needs a connected module");
        test.setBlock(springPos, RevoStorage.SPRING.get());
        var spring = (SpringEntity) test.getBlockEntity(springPos);

        test.succeedWhen(() -> {
            test.assertTrue(spring.charge() == 2048, "Old charge moves into the module");
            test.assertTrue(!terminal.saveWithoutMetadata(registries).contains("Charge"), "The terminal cannot transfer the balance twice");
            var player = viewer(test, "Spring Tester");
            var hit = new BlockHitResult(spring.getBlockPos().getCenter(), Direction.NORTH, spring.getBlockPos(), false);
            spring.getBlockState().useWithoutItem(test.getLevel(), player, hit);
            test.assertTrue(player.containerMenu == player.inventoryMenu && spring.charge() == 2048, "The housing has no menu or built-in manual winding");
            player.setPos(terminal.getBlockPos().getCenter());
            var menu = new StorageMenu(5, player.getInventory(), terminal);
            test.assertTrue(!menu.clickMenuButton(player, 3), "The former Wind button no longer accepts a server action");
            player.setShiftKeyDown(true);
            var terminalHit = new BlockHitResult(terminal.getBlockPos().getCenter(), Direction.NORTH, terminal.getBlockPos(), false);
            var shortcut = terminal.getBlockState().useWithoutItem(test.getLevel(), player, terminalHit);
            test.assertTrue(shortcut == net.minecraft.world.InteractionResult.PASS && spring.charge() == 2048,
                    "Sneak-use on the terminal does not wind the spring");
            test.assertTrue(terminal.spendSpringCharge() && spring.charge() == 2047, "The adjacent terminal spends exactly one charge");
            var tag = spring.saveWithoutMetadata(registries);
            spring.loadWithComponents(tag, registries);
            test.assertTrue(spring.charge() == 2047, "The module retains its charge on reload");
            test.setBlock(springPos, net.minecraft.world.level.block.Blocks.AIR);
            test.assertTrue(!terminal.spendSpringCharge(), "Removing the spring disables crafting immediately");
        });
    }

    @GameTest(template = "storage_test")
    public static void staleGridClickCannotTakeNextItem(GameTestHelper test) {
        var terminalPos = new BlockPos(1, 1, 1);
        var drivePos = terminalPos.east();
        test.setBlock(terminalPos, RevoStorage.TERMINAL.get());
        test.setBlock(drivePos, RevoStorage.DRIVE.get());
        var terminal = (StorageEntity) test.getBlockEntity(terminalPos);
        var drive = (StorageEntity) test.getBlockEntity(drivePos);
        drive.chests.setStackInSlot(0, new ItemStack(Items.CHEST));
        terminal.insert(new ItemStack(Items.APPLE), false);
        terminal.insert(new ItemStack(Items.STICK), false);

        var first = viewer(test, "Storage Tester 1");
        var second = viewer(test, "Storage Tester 2");
        first.setPos(terminal.getBlockPos().getCenter());
        second.setPos(terminal.getBlockPos().getCenter());
        var firstMenu = new StorageMenu(5, first.getInventory(), terminal);
        var secondMenu = new StorageMenu(6, second.getInventory(), terminal);
        firstMenu.broadcastChanges();
        secondMenu.broadcastChanges();
        test.assertTrue(firstMenu.stillValid(first) && secondMenu.stillValid(second),
                "Both viewers can use the live terminal");
        test.assertTrue(!secondMenu.canTakeItemForPickAll(ItemStack.EMPTY, secondMenu.getSlot(46)),
                "Fast repeated grid clicks stay on the custom deposit and withdrawal path");
        int oldRevision = secondMenu.gridRevision();
        firstMenu.clickGrid(first, firstMenu.gridRevision(), 46, 0, false, false);
        test.assertTrue(firstMenu.getCarried().is(Items.APPLE), "First viewer takes the apple");

        secondMenu.broadcastChanges();
        test.assertTrue(secondMenu.getSlot(46).getItem().is(Items.STICK), "Stick moves into the vacated slot");
        secondMenu.clickGrid(second, oldRevision, 46, 0, false, false);
        test.assertTrue(secondMenu.getCarried().isEmpty(), "An in-flight click cannot take the replacement item");
        secondMenu.clickGrid(second, secondMenu.gridRevision(), 46, 0, false, false);
        test.assertTrue(secondMenu.getCarried().is(Items.STICK), "Current selection withdraws the remaining item");
        test.assertTrue(terminal.take(new ItemStack(Items.STICK), 1, true).isEmpty(), "No duplicated item remains");
        secondMenu.clickGrid(second, secondMenu.gridRevision(), 46, 0, false, false);
        test.assertTrue(secondMenu.getCarried().is(Items.STICK), "An earlier withdrawal cannot turn into a deposit");
        secondMenu.clickGrid(second, secondMenu.gridRevision(), 46, 0, false, true);
        test.assertTrue(secondMenu.getCarried().isEmpty() && terminal.take(new ItemStack(Items.STICK), 1, true).is(Items.STICK),
                "The next deliberate deposit returns exactly one stick");

        terminal.insert(new ItemStack(Items.COBBLESTONE, 129), false);
        secondMenu.search = "cobblestone";
        secondMenu.broadcastChanges();
        int cappedRevision = secondMenu.gridRevision();
        terminal.insert(new ItemStack(Items.COBBLESTONE), false);
        secondMenu.broadcastChanges();
        test.assertTrue(secondMenu.getSlot(46).getItem().getCount() == 64
                && secondMenu.gridRevision() == cappedRevision, "A stack above the display cap keeps a stable view");

        test.setBlock(terminalPos, net.minecraft.world.level.block.Blocks.AIR);
        test.setBlock(terminalPos, RevoStorage.TERMINAL.get());
        test.assertTrue(!secondMenu.stillValid(second), "Replacing the block invalidates an earlier viewer");
        test.succeed();
    }
}
