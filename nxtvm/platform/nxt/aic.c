/* Driver for the AT91SAM7's Advanced Interrupt Controller (AIC).
 *
 * The AIC is responsible for queuing interrupts from other
 * peripherals on the board. It then hands them one by one to the ARM
 * CPU core for handling, according to each peripheral's configured
 * priority.
 */

#include "at91sam7.h"

#include "mytypes.h"
#include "interrupts.h"
#include "irq.h"

#include "aic.h"


/* Shorthand for addressing the System Controller structure defined in
 * the AT91 platform package, where the AIC's registers are defined.
 */
#define sysc AT91C_BASE_AIC


/* Initialise the Advanced Interrupt Controller.
 *
 * Note that this function leaves interrupts disabled in the ARM core
 * when it returns, so that other board drivers may register interrupt
 * handlers safely.
 */
void
aic_initialise(void)
{
  int i;

  /* Prevent the ARM core from being interrupted while we set up the
   * AIC.
   */
  interrupts_get_and_disable();

  /* If we're coming from a warm boot, the AIC may be in a weird
   * state. Do some cleaning up to bring the AIC back into a known
   * state:
   *  - All interrupt lines disabled,
   *  - No interrupt lines handled by the FIQ handler,
   *  - No pending interrupts,
   *  - AIC idle, not handling an interrupt.
   */
  sysc->AIC_IDCR = 0xFFFFFFFF;
  sysc->AIC_FFDR = 0xFFFFFFFF;
  sysc->AIC_ICCR = 0xFFFFFFFF;
  sysc->AIC_EOICR = 1;

  /* Enable debug protection. This is necessary for JTAG debugging, so
   * that the hardware debugger can read AIC registers without
   * triggering side-effects.
   */
  sysc->AIC_DCR = 1;

  /* Set default handlers for all interrupt lines. */
  for (i = 0; i < 32; i++) {
    sysc->AIC_SMR[i] = 0;
    sysc->AIC_SVR[i] = (AT91_REG) default_isr;
  }
  sysc->AIC_SVR[AT91C_ID_FIQ] = (AT91_REG) default_fiq;
  sysc->AIC_SPU = (AT91_REG) spurious_isr;
}


/* Register an interrupt service routine for an interrupt line.
 *
 * Note that while this function registers the routine in the AIC, it
 * does not enable or disable the interrupt line for that vector. Use
 * aic_mask_on and aic_mask_off to control actual activation of the
 * interrupt line.
 *
 * Args:
 *   vector: The peripheral ID to claim (see at91sam7.h for peripheral IDs)
 *   mode: The priority of this interrupt in relation to others. See aic.h
 *         for a list of defined values.
 *   isr: A pointer to the interrupt service routine function.
 */
void
aic_set_vector(U32 vector, U32 mode, void (*isr)(void))
{
  if (vector < 32) {
    int i_state = interrupts_get_and_disable();

    sysc->AIC_SMR[vector] = mode;
    sysc->AIC_SVR[vector] = (AT91_REG) isr;
    if (i_state)
      interrupts_enable();
  }
}


/* Enable handling of an interrupt line in the AIC.
 *
 * Args:
 *   vector: The peripheral ID of the interrupt line to enable.
 */
void
aic_mask_on(U32 vector)
{
  int i_state = interrupts_get_and_disable();

  sysc->AIC_IECR = (1 << vector);
  if (i_state)
    interrupts_enable();
}


/* Disable handling of an interrupt line in the AIC.
 *
 * Args:
 *   vector: The peripheral ID of the interrupt line to disable.
 */
void
aic_mask_off(U32 vector)
{
  int i_state = interrupts_get_and_disable();

  sysc->AIC_IDCR = (1 << vector);
  if (i_state)
    interrupts_enable();
}


/* Clear an interrupt line in the AIC.
 *
 * Args:
 *   vector: The peripheral ID of the interrupt line to clear.
 */
void
aic_clear(U32 vector)
{
  int i_state = interrupts_get_and_disable();

  sysc->AIC_ICCR = (1 << vector);
  if (i_state)
    interrupts_enable();
}
