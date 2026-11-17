package dk.revo.revoindustry.feeding;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.geom.PartPose;
/** Generated from the native Blockbench sources by scripts/build_feeding_helmet_models.py. */
public final class FeedingHelmetGeometry {
 public static LayerDefinition create(int tier) {
  var mesh = new MeshDefinition(); var root = mesh.getRoot();
  for (String name : new String[]{"head","hat","body","right_arm","left_arm","right_leg","left_leg"}) root.addOrReplaceChild(name,CubeListBuilder.create(),PartPose.ZERO);
  var head = root.getChild("head");
  switch (tier) {
   case 0 -> {
    head.addOrReplaceChild("crown",CubeListBuilder.create().texOffs(2,2).addBox(-4f,-9.5f,-4f,8f,1.5f,8f),PartPose.ZERO);
    head.addOrReplaceChild("helmet_top",CubeListBuilder.create().texOffs(38,2).addBox(-4.7f,-8f,-4.5f,9.4f,1.2f,9.0f),PartPose.ZERO);
    head.addOrReplaceChild("shell_side_-4.8",CubeListBuilder.create().texOffs(78,2).addBox(-4.8f,-6.8f,-3f,0.9f,3.8f,7.5f),PartPose.ZERO);
    head.addOrReplaceChild("shell_side_3.9",CubeListBuilder.create().texOffs(98,2).addBox(3.9f,-6.8f,-3f,0.9f,3.8f,7.5f),PartPose.ZERO);
    head.addOrReplaceChild("rear_shell",CubeListBuilder.create().texOffs(2,17).addBox(-3.9f,-6.8f,3.8f,7.8f,4.1f,1.0f),PartPose.ZERO);
    head.addOrReplaceChild("brow_brim",CubeListBuilder.create().texOffs(23,17).addBox(-4.6f,-6.8f,-5.1f,9.2f,0.7f,1.6f),PartPose.ZERO);
    head.addOrReplaceChild("ration_module",CubeListBuilder.create().texOffs(48,17).addBox(4.8f,-7.4f,0.2f,2.0f,2.8f,3.0f),PartPose.ZERO);
    head.addOrReplaceChild("hose_coupling",CubeListBuilder.create().texOffs(62,17).addBox(4.8f,-4.6f,-1.4f,1.2f,1.0f,2.3f),PartPose.ZERO);
    head.addOrReplaceChild("hose_down",CubeListBuilder.create().texOffs(73,17).addBox(4.85f,-3.6f,-2.1f,0.9f,2.2f,0.9f),PartPose.ZERO);
    head.addOrReplaceChild("hose_to_face",CubeListBuilder.create().texOffs(80,17).addBox(4.85f,-1.4f,-4.9f,0.9f,0.9f,3.7f),PartPose.ZERO);
    head.addOrReplaceChild("hose_to_mouth",CubeListBuilder.create().texOffs(93,17).addBox(0.8f,-1.4f,-5f,4.05f,0.9f,0.9f),PartPose.ZERO);
    head.addOrReplaceChild("mouthpiece",CubeListBuilder.create().texOffs(106,17).addBox(-0.8f,-1.6f,-5.3f,2.0f,1.3f,0.9f),PartPose.ZERO);
    head.addOrReplaceChild("left_strap",CubeListBuilder.create().texOffs(115,17).addBox(-4.9f,-3f,1.5f,0.8f,1.9f,0.9f),PartPose.ZERO);
   }
   case 1 -> {
    head.addOrReplaceChild("crown",CubeListBuilder.create().texOffs(2,2).addBox(-4f,-9.5f,-4f,8f,1.5f,8f),PartPose.ZERO);
    head.addOrReplaceChild("helmet_top",CubeListBuilder.create().texOffs(38,2).addBox(-4.7f,-8f,-4.5f,9.4f,1.2f,9.0f),PartPose.ZERO);
    head.addOrReplaceChild("shell_side_-4.8",CubeListBuilder.create().texOffs(78,2).addBox(-4.8f,-6.8f,-3f,0.9f,3.8f,7.5f),PartPose.ZERO);
    head.addOrReplaceChild("shell_side_3.9",CubeListBuilder.create().texOffs(98,2).addBox(3.9f,-6.8f,-3f,0.9f,3.8f,7.5f),PartPose.ZERO);
    head.addOrReplaceChild("rear_shell",CubeListBuilder.create().texOffs(2,17).addBox(-3.9f,-6.8f,3.8f,7.8f,4.1f,1.0f),PartPose.ZERO);
    head.addOrReplaceChild("brow_brim",CubeListBuilder.create().texOffs(23,17).addBox(-4.6f,-6.8f,-5.1f,9.2f,0.7f,1.6f),PartPose.ZERO);
    head.addOrReplaceChild("ration_module",CubeListBuilder.create().texOffs(48,17).addBox(4.8f,-7.4f,0.2f,2.0f,2.8f,3.0f),PartPose.ZERO);
    head.addOrReplaceChild("hose_coupling",CubeListBuilder.create().texOffs(62,17).addBox(4.8f,-4.6f,-1.4f,1.2f,1.0f,2.3f),PartPose.ZERO);
    head.addOrReplaceChild("hose_down",CubeListBuilder.create().texOffs(73,17).addBox(4.85f,-3.6f,-2.1f,0.9f,2.2f,0.9f),PartPose.ZERO);
    head.addOrReplaceChild("hose_to_face",CubeListBuilder.create().texOffs(80,17).addBox(4.85f,-1.4f,-4.9f,0.9f,0.9f,3.7f),PartPose.ZERO);
    head.addOrReplaceChild("hose_to_mouth",CubeListBuilder.create().texOffs(93,17).addBox(0.8f,-1.4f,-5f,4.05f,0.9f,0.9f),PartPose.ZERO);
    head.addOrReplaceChild("mouthpiece",CubeListBuilder.create().texOffs(106,17).addBox(-0.8f,-1.6f,-5.3f,2.0f,1.3f,0.9f),PartPose.ZERO);
    head.addOrReplaceChild("left_strap",CubeListBuilder.create().texOffs(115,17).addBox(-4.9f,-3f,1.5f,0.8f,1.9f,0.9f),PartPose.ZERO);
    head.addOrReplaceChild("left_storage_pod",CubeListBuilder.create().texOffs(2,26).addBox(-6.5f,-6.6f,-0.7f,1.7f,3.3f,3.6f),PartPose.ZERO);
    head.addOrReplaceChild("reinforced_crown_rail",CubeListBuilder.create().texOffs(16,26).addBox(-0.6f,-10.1f,-2.8f,1.2f,0.6f,6.0f),PartPose.ZERO);
    head.addOrReplaceChild("side_control",CubeListBuilder.create().texOffs(34,26).addBox(6.8f,-6.8f,0.7f,0.55f,1.8f,2.1f),PartPose.ZERO);
    head.addOrReplaceChild("front_mount",CubeListBuilder.create().texOffs(43,26).addBox(-1f,-8.1f,-5.3f,2f,1.1f,0.8f),PartPose.ZERO);
    head.addOrReplaceChild("hose_armour",CubeListBuilder.create().texOffs(52,26).addBox(5.75f,-3.3f,-2f,0.5f,2.0f,0.7f),PartPose.ZERO);
   }
   case 2 -> {
    head.addOrReplaceChild("crown",CubeListBuilder.create().texOffs(2,2).addBox(-4f,-9.5f,-4f,8f,1.5f,8f),PartPose.ZERO);
    head.addOrReplaceChild("helmet_top",CubeListBuilder.create().texOffs(38,2).addBox(-4.7f,-8f,-4.5f,9.4f,1.2f,9.0f),PartPose.ZERO);
    head.addOrReplaceChild("shell_side_-4.8",CubeListBuilder.create().texOffs(78,2).addBox(-4.8f,-6.8f,-3f,0.9f,3.8f,7.5f),PartPose.ZERO);
    head.addOrReplaceChild("shell_side_3.9",CubeListBuilder.create().texOffs(98,2).addBox(3.9f,-6.8f,-3f,0.9f,3.8f,7.5f),PartPose.ZERO);
    head.addOrReplaceChild("rear_shell",CubeListBuilder.create().texOffs(2,17).addBox(-3.9f,-6.8f,3.8f,7.8f,4.1f,1.0f),PartPose.ZERO);
    head.addOrReplaceChild("brow_brim",CubeListBuilder.create().texOffs(23,17).addBox(-4.6f,-6.8f,-5.1f,9.2f,0.7f,1.6f),PartPose.ZERO);
    head.addOrReplaceChild("ration_module",CubeListBuilder.create().texOffs(48,17).addBox(4.8f,-7.4f,0.2f,2.0f,2.8f,3.0f),PartPose.ZERO);
    head.addOrReplaceChild("hose_coupling",CubeListBuilder.create().texOffs(62,17).addBox(4.8f,-4.6f,-1.4f,1.2f,1.0f,2.3f),PartPose.ZERO);
    head.addOrReplaceChild("hose_down",CubeListBuilder.create().texOffs(73,17).addBox(4.85f,-3.6f,-2.1f,0.9f,2.2f,0.9f),PartPose.ZERO);
    head.addOrReplaceChild("hose_to_face",CubeListBuilder.create().texOffs(80,17).addBox(4.85f,-1.4f,-4.9f,0.9f,0.9f,3.7f),PartPose.ZERO);
    head.addOrReplaceChild("hose_to_mouth",CubeListBuilder.create().texOffs(93,17).addBox(0.8f,-1.4f,-5f,4.05f,0.9f,0.9f),PartPose.ZERO);
    head.addOrReplaceChild("mouthpiece",CubeListBuilder.create().texOffs(106,17).addBox(-0.8f,-1.6f,-5.3f,2.0f,1.3f,0.9f),PartPose.ZERO);
    head.addOrReplaceChild("left_strap",CubeListBuilder.create().texOffs(115,17).addBox(-4.9f,-3f,1.5f,0.8f,1.9f,0.9f),PartPose.ZERO);
    head.addOrReplaceChild("left_storage_pod",CubeListBuilder.create().texOffs(2,26).addBox(-6.5f,-6.6f,-0.7f,1.7f,3.3f,3.6f),PartPose.ZERO);
    head.addOrReplaceChild("reinforced_crown_rail",CubeListBuilder.create().texOffs(16,26).addBox(-0.6f,-10.1f,-2.8f,1.2f,0.6f,6.0f),PartPose.ZERO);
    head.addOrReplaceChild("side_control",CubeListBuilder.create().texOffs(34,26).addBox(6.8f,-6.8f,0.7f,0.55f,1.8f,2.1f),PartPose.ZERO);
    head.addOrReplaceChild("front_mount",CubeListBuilder.create().texOffs(43,26).addBox(-1f,-8.1f,-5.3f,2f,1.1f,0.8f),PartPose.ZERO);
    head.addOrReplaceChild("hose_armour",CubeListBuilder.create().texOffs(52,26).addBox(5.75f,-3.3f,-2f,0.5f,2.0f,0.7f),PartPose.ZERO);
    head.addOrReplaceChild("rear_storage_manifold",CubeListBuilder.create().texOffs(58,26).addBox(-3.6f,-7.5f,4.8f,7.2f,3.8f,1.4f),PartPose.ZERO);
    head.addOrReplaceChild("ration_canister_-3.1",CubeListBuilder.create().texOffs(79,26).addBox(-3.1f,-7.4f,6.2f,1.6f,3.9f,1.1f),PartPose.ZERO);
    head.addOrReplaceChild("ration_canister_-0.8",CubeListBuilder.create().texOffs(88,26).addBox(-0.8f,-7.4f,6.2f,1.6f,3.9f,1.1f),PartPose.ZERO);
    head.addOrReplaceChild("ration_canister_1.5",CubeListBuilder.create().texOffs(97,26).addBox(1.5f,-7.4f,6.2f,1.6f,3.9f,1.1f),PartPose.ZERO);
    head.addOrReplaceChild("left_temple_plate",CubeListBuilder.create().texOffs(106,26).addBox(-5.25f,-7.5f,-3.1f,0.454f,2.0f,3.2f),PartPose.ZERO);
    head.addOrReplaceChild("right_temple_plate",CubeListBuilder.create().texOffs(117,26).addBox(4.8f,-7.5f,-3.1f,0.45f,2.0f,3.2f),PartPose.ZERO);
    head.addOrReplaceChild("status_panel",CubeListBuilder.create().texOffs(2,36).addBox(-2.2f,-7.9f,-5.4f,4.4f,0.8f,0.3f),PartPose.ZERO);
   }
   default -> throw new IllegalArgumentException("Unknown helmet tier");
  }
  return LayerDefinition.create(mesh,128,128);
 }
}
