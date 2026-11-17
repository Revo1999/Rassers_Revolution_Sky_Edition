// Revo's Creationalization: small, additive Create <-> MI recipe bridges.
// Edit this file and /reload. No Java build or KubeJS Create addon is needed.
// Keep bootstrap crafting intact. Progression comes from the actual machine and
// ingredients; an ID or comment such as "mv" does NOT enforce a voltage gate.
ServerEvents.recipes(event => {
  if (!Platform.isLoaded('create') || !Platform.isLoaded('modern_industrialization')) return

  // MI duration is in ticks. EU is the recipe's EU/t requirement.
  // item_inputs uses MI's own JSON format (amount + item OR tag).
  const assemble = (name, inputs, output, duration, eu) => {
    event.custom({
      type: 'modern_industrialization:assembler',
      duration: duration,
      eu: eu,
      item_inputs: inputs,
      item_outputs: [{ item: output, amount: 1 }]
    }).id('revo_creationalization:assembler/' + name)
  }

  // Exactly the native Create crafting costs, with no yield bonus or recycling.
  // The existing crafting routes remain available before the MI Assembler.
  assemble('brass_hand', [
    { item: 'create:andesite_alloy', amount: 1 },
    { tag: 'c:plates/brass', amount: 4 }
  ], 'create:brass_hand', 100, 8)

  assemble('whisk', [
    { item: 'create:andesite_alloy', amount: 2 },
    { tag: 'c:plates/iron', amount: 5 }
  ], 'create:whisk', 100, 8)
})
