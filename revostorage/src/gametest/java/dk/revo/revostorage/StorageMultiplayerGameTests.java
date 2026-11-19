package dk.revo.revostorage;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(RevoStorage.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StorageMultiplayerGameTests {
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

        var first = test.makeMockServerPlayerInLevel();
        var second = test.makeMockServerPlayerInLevel();
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
